-- Enforce fixed portfolio themes for the public frontend (comic | minimalist | dark-tech).
--
-- Backfill mapping:
--   NULL / empty / 'default'  → comic
--   'hacker', 'dark-hacker', 'terminal' → dark-tech
--   already valid (comic | minimalist | dark-tech) → kept (normalized to lowercase)
--   anything else unknown → comic
--
-- New default for inserts: comic. Column length set to 20 (>= 20 required).

UPDATE portfolios
SET theme = CASE
    WHEN theme IS NULL
         OR TRIM(theme) = ''
         OR LOWER(TRIM(theme)) = 'default'
        THEN 'comic'
    WHEN LOWER(TRIM(theme)) IN ('hacker', 'dark-hacker', 'terminal')
        THEN 'dark-tech'
    WHEN LOWER(TRIM(theme)) IN ('comic', 'minimalist', 'dark-tech')
        THEN LOWER(TRIM(theme))
    ELSE 'comic'
END;

ALTER TABLE portfolios ALTER COLUMN theme TYPE VARCHAR(20);
ALTER TABLE portfolios ALTER COLUMN theme SET DEFAULT 'comic';
ALTER TABLE portfolios ALTER COLUMN theme SET NOT NULL;
