#!/usr/bin/env python3
"""Exercise every application route against disposable API test accounts.

Usage: python3 scripts/api_smoke_test.py [--base-url http://127.0.0.1:18080]
Creates two accounts and a portfolio; use a disposable database. No email is sent.
Results omit credentials and tokens and are saved under target/.
"""
import argparse
import base64
import json
import secrets
import socket
import ssl
import time
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--base-url', default='http://127.0.0.1:18080')
    parser.add_argument('--database-label', default='Server-configured database',
                        help='Database environment recorded in the results')
    parser.add_argument('--seed-redis-otp', action='store_true',
                        help='Seed an OTP for the new test user in .env Redis; requires bcrypt')
    args = parser.parse_args()
    base = args.base_url.rstrip('/')
    results = []
    run = secrets.token_hex(4)
    account = dict(username='test' + run, email=f'api-{run}@example.com',
                   password=secrets.token_urlsafe(20))
    slug = 'api-test-' + run

    def check(label, method, path, expected=200, data=None, token=None,
              raw=None, content_type=None, verify=None):
        headers = {}
        if token:
            headers['Authorization'] = 'Bearer ' + token
        if raw is not None:
            body = raw
            headers['Content-Type'] = content_type
        elif data is not None:
            body = json.dumps(data).encode()
            headers['Content-Type'] = 'application/json'
        else:
            body = None
        start = time.monotonic()
        try:
            request = Request(base + path, data=body, headers=headers, method=method)
            try:
                response = urlopen(request, timeout=90)
            except HTTPError as error:
                response = error
            with response:
                status = response.code
                payload = response.read()
                try:
                    value = json.loads(payload)
                except (ValueError, UnicodeDecodeError):
                    value = payload
            passed = status == expected
            detail = ''
            if passed and verify:
                passed = bool(verify(value))
                if not passed:
                    detail = 'Response content assertion failed'
            if status != expected:
                detail = value.get('message', '') if isinstance(value, dict) else 'Unexpected HTTP status'
        except (URLError, TimeoutError, OSError) as error:
            status, value, passed, detail = None, {}, False, type(error).__name__
        results.append(dict(check=label, method=method, path=path,
                            expected=expected, actual=status, passed=passed,
                            detail=detail, seconds=round(time.monotonic()-start, 3)))
        print(f'{"PASS" if passed else "FAIL"} {label}: {status} (expected {expected}) {detail}', flush=True)
        return value

    def upload(label, token, content, mime='image/png', expected=200):
        boundary = 'SmokeBoundary' + run
        raw = (f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="test.png"\r\n'
               f'Content-Type: {mime}\r\n\r\n').encode() + content + f'\r\n--{boundary}--\r\n'.encode()
        return check(label, 'POST', '/api/me/portfolio/image', expected, token=token,
                     raw=raw, content_type='multipart/form-data; boundary=' + boundary)

    check('Liveness', 'GET', '/health', verify=lambda v: v['status'] == 'UP')
    check('Dependency health', 'GET', '/actuator/health', verify=lambda v: v['status'] == 'UP')
    check('Actuator info', 'GET', '/actuator/info')
    check('Reject invalid signup', 'POST', '/api/auth/signup', 400, data={})
    user = check('Create test user', 'POST', '/api/auth/signup', 201, data=account,
                 verify=lambda v: bool(v.get('id') and v.get('token')))
    token = user.get('token')
    if not token:
        raise SystemExit('Cannot continue without a test user')
    check('Reject duplicate signup', 'POST', '/api/auth/signup', 409, data=account)
    check('Sign in by email', 'POST', '/api/auth/signin', data={
        'email': account['email'], 'password': account['password']},
        verify=lambda v: v.get('signinSuccessful') and bool(v.get('token')))
    check('Sign in by username', 'POST', '/api/auth/signin', data={
        'username': account['username'], 'password': account['password']},
        verify=lambda v: v.get('signinSuccessful') and bool(v.get('token')))
    check('Reject wrong password', 'POST', '/api/auth/signin', 401,
          data={'email': account['email'], 'password': 'WrongPassword123'})
    check('Reject anonymous portfolio read', 'GET', '/api/me/portfolio', 401)
    check('Reject invalid JWT', 'GET', '/api/me/portfolio', 401, token='invalid.token.value')
    check('Missing own portfolio', 'GET', '/api/me/portfolio', 404, token=token)
    portfolio = dict(slug=slug, fullName='API Test User', publicEmail=account['email'],
                     phone='+910000000000', githubUrl='https://github.com/example',
                     linkedinUrl='https://www.linkedin.com/in/example',
                     theme='comic', primaryColor='#123456', secondaryColor='#abcdef')
    check('Reject anonymous portfolio create', 'POST', '/api/portfolios', 401, data=portfolio)
    check('Reject invalid portfolio', 'POST', '/api/portfolios', 400, data={}, token=token)
    check('Create portfolio', 'POST', '/api/portfolios', 201, data=portfolio, token=token)
    check('Reject duplicate portfolio', 'POST', '/api/portfolios', 409, data=portfolio, token=token)
    for theme in ('minimalist', 'dark-tech', 'comic'):
        portfolio['theme'] = theme
        check('Update portfolio theme ' + theme, 'PUT', '/api/portfolios',
              data=portfolio, token=token, verify=lambda v, t=theme: v.get('theme') == t)
    check('Reject unsupported theme', 'PUT', '/api/portfolios', 400,
          data={**portfolio, 'theme': 'unsupported'}, token=token)
    check('Reject invalid slug', 'PUT', '/api/portfolios', 400,
          data={**portfolio, 'slug': 'Invalid Slug'}, token=token)
    check('Read public portfolio', 'GET', '/api/portfolios/' + slug,
          verify=lambda v: v.get('fullName') == portfolio['fullName'])
    check('Missing public portfolio', 'GET', '/api/portfolios/not-found-' + run, 404)
    check('Create summary', 'PUT', '/api/me/portfolio/summary',
          data={'summary': 'Initial API test summary'}, token=token)
    check('Update summary', 'PUT', '/api/me/portfolio/summary',
          data={'summary': 'Updated API test summary'}, token=token)
    check('Reject empty summary', 'PUT', '/api/me/portfolio/summary', 400,
          data={'summary': ''}, token=token)
    other = check('Create isolation test user', 'POST', '/api/auth/signup', 201,
                  data={**account, 'username': 'other' + run, 'email': f'other-{run}@example.com'})
    other_token = other.get('token')
    check('Create isolation portfolio', 'POST', '/api/portfolios', 201,
          data={**portfolio, 'slug': 'other-' + slug}, token=other_token)
    check('Reject another users slug', 'PUT', '/api/portfolios', 409,
          data=portfolio, token=other_token)
    sections = [
        ('projects', 'projects', 'title', {'title': 'Test Project', 'description': 'API test', 'techStack': 'Java', 'displayOrder': 2}),
        ('experiences', 'experiences', 'companyName', {'companyName': 'Test Company', 'jobTitle': 'Developer', 'startDate': '2025-01-01', 'currentlyWorking': True, 'displayOrder': 2}),
        ('certifications', 'certifications', 'name', {'name': 'Test Certificate', 'issuingOrganization': 'Test Org', 'issueDate': '2025-01-01', 'displayOrder': 2}),
        ('technical-skills', 'technicalSkills', 'skillName', {'skillName': 'Java', 'category': 'Backend', 'displayOrder': 2}),
        ('custom-links', 'customLinks', 'label', {'label': 'Test Link', 'url': 'https://example.com', 'displayOrder': 2}),
    ]
    created = []
    for route, field, key, payload in sections:
        path = '/api/me/portfolio/' + route
        check('Reject invalid ' + route, 'POST', path, 400, data={}, token=token)
        item = check('Create ' + route, 'POST', path, 201, data=payload, token=token)
        if not item.get('id'):
            continue
        item_path = path + '/' + str(item['id'])
        updated = {**payload, key: payload[key] + ' Updated', 'displayOrder': 1}
        check('Update ' + route, 'PUT', item_path, data=updated, token=token)
        check('Read persisted ' + route, 'GET', '/api/me/portfolio', token=token,
              verify=lambda v, f=field, k=key, p=updated: any(x.get(k) == p[k] for x in v[f]))
        check('Reject cross-user update ' + route, 'PUT', item_path, 404, data=updated, token=other_token)
        check('Reject cross-user delete ' + route, 'DELETE', item_path, 404, token=other_token)
        check('Reject missing update ' + route, 'PUT', path + '/9223372036854775807', 404, data=updated, token=token)
        created.append((route, field, item_path))
    check('Read combined public portfolio', 'GET', '/api/portfolios/' + slug,
          verify=lambda v: v.get('summary') == 'Updated API test summary' and
          all(len(v[field]) == 1 for _, field, _, _ in sections))
    png = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=')
    upload('Reject non-image upload', token, b'plain text', 'text/plain', 400)
    image = upload('Upload profile image', token, png)
    image_url = image.get('imageUrl')
    media_path = None
    if image_url:
        marker = image_url.find('profiles/')
        if marker >= 0:
            media_path = '/api/media/' + image_url[marker:]
            check('Read uploaded media bytes', 'GET', media_path, verify=lambda v: v == png)
        check('Image persisted in portfolio', 'GET', '/api/me/portfolio', token=token,
              verify=lambda v: v.get('profileImageUrl') == image_url)
        upload('Replace profile image', token, png)
        if media_path:
            check('Old image removed from storage', 'GET', media_path, 404)
    check('Delete profile image', 'DELETE', '/api/me/portfolio/image', 204, token=token)
    check('Image removed from portfolio', 'GET', '/api/me/portfolio', token=token,
          verify=lambda v: v.get('profileImageUrl') is None)
    check('Reject invalid media path', 'GET', '/api/media/invalid-key', 404)
    check('Missing media object', 'GET', f'/api/media/profiles/{user["id"]}/missing-{run}.png', 404)
    check('Reject empty AI text', 'POST', '/api/me/portfolio/suggest', 400, data={'text': ''}, token=token)
    check('Generate AI suggestion', 'POST', '/api/me/portfolio/suggest',
          data={'text': 'My name is API Test User. I am a Java and Spring Boot developer. I built an API testing tool using Python.'},
          token=token, verify=lambda v: bool(v.get('fullName')) and isinstance(v.get('projects'), list)
          and isinstance(v.get('technicalSkills'), list))
    check('Forgot password unknown user (no email)', 'POST', '/api/auth/forgot-password',
          data={'email': f'unknown-{run}@example.com'}, verify=lambda v: 'If that email exists' in v.get('message', ''))
    check('Reject invalid forgot password', 'POST', '/api/auth/forgot-password', 400, data={'email': 'invalid'})
    check('Reject malformed OTP', 'POST', '/api/auth/reset-password', 400,
          data={'email': account['email'], 'otp': 'bad', 'newPassword': 'NewTestPassword123!'})
    check('Reject missing OTP in Redis', 'POST', '/api/auth/reset-password', 400,
          data={'email': account['email'], 'otp': '000000', 'newPassword': 'NewTestPassword123!'})
    if args.seed_redis_otp:
        import bcrypt
        env = {}
        for raw_line in Path('.env').read_text().splitlines():
            line = raw_line.strip()
            if line and not line.startswith('#') and '=' in line:
                key, value = line.split('=', 1)
                env[key.strip()] = value.strip().strip('"').strip("'")
        otp = f'{secrets.randbelow(1000000):06d}'
        otp_hash = bcrypt.hashpw(otp.encode(), bcrypt.gensalt()).decode()
        connection = socket.create_connection((env['UPSTASH_REDIS_HOST'],
                                              int(env.get('UPSTASH_REDIS_PORT', 6379))), timeout=15)
        if env.get('UPSTASH_REDIS_SSL', 'true').lower() == 'true':
            connection = ssl.create_default_context().wrap_socket(
                connection, server_hostname=env['UPSTASH_REDIS_HOST'])
        stream = connection.makefile('rb')

        def redis(*parts):
            encoded = [str(p).encode() for p in parts]
            connection.sendall(f'*{len(encoded)}\r\n'.encode() + b''.join(
                f'${len(p)}\r\n'.encode() + p + b'\r\n' for p in encoded))
            reply = stream.readline()
            if reply.startswith(b'-') or not reply:
                raise RuntimeError('Redis command failed')
            return reply

        keys = [prefix + account['email'] for prefix in
                ('password-reset:otp:', 'password-reset:attempts:', 'password-reset:cooldown:')]
        try:
            redis('AUTH', env.get('UPSTASH_REDIS_USERNAME', 'default'), env['UPSTASH_REDIS_PASSWORD'])
            redis('SET', keys[0], otp_hash, 'EX', 300)
            redis('DEL', keys[1])
            redis('SET', keys[2], '1', 'EX', 60)
            new_password = secrets.token_urlsafe(20)
            reset = {'email': account['email'], 'otp': otp, 'newPassword': new_password}
            check('Reset password with valid seeded OTP', 'POST', '/api/auth/reset-password',
                  data=reset, verify=lambda v: v.get('message') == 'Password reset successfully')
            check('Reject old password after reset', 'POST', '/api/auth/signin', 401,
                  data={'email': account['email'], 'password': account['password']})
            check('Sign in with reset password', 'POST', '/api/auth/signin',
                  data={'email': account['email'], 'password': new_password},
                  verify=lambda v: v.get('signinSuccessful') and bool(v.get('token')))
            check('Reject OTP replay', 'POST', '/api/auth/reset-password', 400, data=reset)
        finally:
            redis('DEL', *keys)
            stream.close()
            connection.close()
    for route, field, path in created:
        check('Delete ' + route, 'DELETE', path, 204, token=token)
        check('Reject repeated delete ' + route, 'DELETE', path, 404, token=token)
        check('Verify deleted ' + route, 'GET', '/api/me/portfolio', token=token,
              verify=lambda v, f=field: not v[f])
    report = dict(base_url=base, database=args.database_label,
                  test_username=account['username'], test_user_id=user.get('id'),
                  passed=sum(r['passed'] for r in results), failed=sum(not r['passed'] for r in results),
                  limitations=['Forgot-password for an existing user and email delivery were not exercised; no email was sent.'] +
                  ([] if args.seed_redis_otp else ['Successful OTP reset was not exercised.']),
                  results=results)
    Path('target').mkdir(exist_ok=True)
    Path('target/api-smoke-results.json').write_text(json.dumps(report, indent=2) + '\n')
    print(f'\n{report["passed"]} passed, {report["failed"]} failed. Report: target/api-smoke-results.json')
    return bool(report['failed'])


if __name__ == '__main__':
    raise SystemExit(main())
