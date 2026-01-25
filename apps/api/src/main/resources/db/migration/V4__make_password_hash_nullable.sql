-- V4__make_password_hash_nullable.sql
-- Allow OAuth customers to have no password

ALTER TABLE customers ALTER COLUMN password_hash DROP NOT NULL;
