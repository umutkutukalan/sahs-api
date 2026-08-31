-- Kolon boyutunu yeni uzun değerleri alabilecek şekilde genişletelim
ALTER TABLE post_reactions
    MODIFY COLUMN reaction_type VARCHAR(50);
-- Sonrasında güncelleme işlemini yapalım
UPDATE
    post_reactions pr
JOIN
    posts p
ON
    pr.post_id = p.id
SET
    pr.reaction_type =
    CASE
        WHEN
            p.post_type = 'SAHNE'
        THEN 'SHINE_SAHNE'
        WHEN
            p.post_type = 'MONOLOG'
        THEN 'SHINE_MONOLOG'
        WHEN
            p.post_type = 'YANYANA'
        THEN 'SHINE_YANYANA'
        WHEN
            p.post_type = 'TERSYUZ'
        THEN 'SHINE_TERSYUZ'
        ELSE 'SHINE_SAHNE'
    END
WHERE
    pr.reaction_type = 'SHINE';