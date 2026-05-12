CREATE TYPE "user_roles" as ENUM (
    'USER',
    'ADMIN'
);

ALTER TABLE "users" ADD COLUMN "role" user_roles NOT NULL DEFAULT 'USER';