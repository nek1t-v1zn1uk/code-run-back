ALTER TABLE problems ALTER COLUMN difficulty TYPE INTEGER USING (
    CASE difficulty::text
        WHEN 'very_easy' THEN 0
        WHEN 'easy' THEN 1
        WHEN 'medium' THEN 2
        WHEN 'hard' THEN 3
        WHEN 'very_hard' THEN 4
        ELSE 0
        END
    );
CREATE INDEX idx_problems_difficulty_id ON problems (difficulty, id);