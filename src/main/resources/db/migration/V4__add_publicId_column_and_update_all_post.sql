UPDATE
    posts
SET
    public_id = SUBSTRING(MD5(CONCAT(UUID(), id)), 1, 12)
WHERE
    public_id = '';