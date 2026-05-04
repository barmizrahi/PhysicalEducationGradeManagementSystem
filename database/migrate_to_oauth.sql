-- Migration script to update teachers table for Google OAuth authentication
-- This script removes password-based authentication and adds OAuth fields

-- Add new columns for OAuth
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS picture VARCHAR(500);
ALTER TABLE teachers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- For existing teachers, copy username to email if email is null
-- This is a temporary measure to preserve data during migration
UPDATE teachers SET email = username WHERE email IS NULL;

-- Add unique constraint on email
ALTER TABLE teachers ADD CONSTRAINT teachers_email_unique UNIQUE (email);

-- Add unique constraint on google_id (allowing nulls)
CREATE UNIQUE INDEX IF NOT EXISTS teachers_google_id_unique ON teachers (google_id) WHERE google_id IS NOT NULL;

-- Drop the old username unique constraint if it exists
ALTER TABLE teachers DROP CONSTRAINT IF EXISTS teachers_username_key;

-- Drop the password_hash column (WARNING: This will remove all password data)
-- Comment out this line if you want to keep passwords temporarily during migration
ALTER TABLE teachers DROP COLUMN IF EXISTS password_hash;

-- Drop the username column (WARNING: This will remove username data)
-- Comment out this line if you want to keep usernames temporarily during migration
ALTER TABLE teachers DROP COLUMN IF EXISTS username;

-- Set updated_at for existing records
UPDATE teachers SET updated_at = created_at WHERE updated_at IS NULL;

-- Make email NOT NULL after data migration
ALTER TABLE teachers ALTER COLUMN email SET NOT NULL;

COMMENT ON COLUMN teachers.email IS 'Teacher email address from Google OAuth (unique identifier)';
COMMENT ON COLUMN teachers.google_id IS 'Google user ID from OAuth';
COMMENT ON COLUMN teachers.picture IS 'Profile picture URL from Google';
COMMENT ON COLUMN teachers.updated_at IS 'Timestamp of last profile update';
