-- Fix raw_result column to allow NULL values
-- This allows saving notes without a test result (e.g., student was absent)

ALTER TABLE test_results 
ALTER COLUMN raw_result DROP NOT NULL;

-- Update existing records with NULL raw_result to have calculated_grade = 0
UPDATE test_results 
SET calculated_grade = 0 
WHERE raw_result IS NULL AND calculated_grade IS NULL;
