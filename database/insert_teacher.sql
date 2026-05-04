-- Update test teacher with BCrypt hashed password
-- Password: password123
-- BCrypt hash generated with strength 10

UPDATE teachers 
SET password_hash = '$2a$10$dXJ3SW6G7P370U9.9OFnNOKgdUjIpjH/.DsOWdAsATacQg5PsRfby'
WHERE username = 'teacher1';

-- Verify the teacher was updated
SELECT id, username, full_name, LENGTH(password_hash) as hash_length, password_hash FROM teachers WHERE username='teacher1';
