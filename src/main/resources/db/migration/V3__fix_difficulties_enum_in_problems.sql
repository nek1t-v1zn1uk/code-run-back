ALTER TABLE problems ALTER COLUMN difficulty TYPE INTEGER USING (
    CASE difficulty::text
        WHEN 'VERY_EASY' THEN 0
        WHEN 'EASY' THEN 1
        WHEN 'MEDIUM' THEN 2
        WHEN 'HARD' THEN 3
        WHEN 'VERY_HARD' THEN 4
        ELSE 0
        END
    );
CREATE INDEX idx_problems_difficulty_id ON problems (difficulty, id);