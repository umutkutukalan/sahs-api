UPDATE
    posts
SET
    cover_images = JSON_ARRAY(cover_image)
WHERE
    cover_images IS NULL
AND cover_image IS NOT NULL;