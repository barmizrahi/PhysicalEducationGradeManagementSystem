-- Add penalty_unit column to tests table
-- For PENALTY + TIME tests this defines the deduction interval (in decimal minutes).
-- Example: 0.75 = every 45 seconds over the target deducts penalty_per_unit points.
-- Formula: grade = 100 - ((raw_result - target_value) / penalty_unit) * penalty_per_unit
-- Defaults to 1.0 (one minute) which reproduces the previous per-unit penalty behavior.

ALTER TABLE tests
ADD COLUMN IF NOT EXISTS penalty_unit DOUBLE PRECISION DEFAULT 1.0;

-- Backfill existing PENALTY tests that have no penalty_unit set
UPDATE tests
SET penalty_unit = 1.0
WHERE penalty_unit IS NULL;
