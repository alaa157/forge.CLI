# Authentication API

## Registration

`POST /api/v1/auth/register`

Email is trimmed and normalized to lowercase. Passwords are validated and stored only as Argon2 hashes. The response never contains a password or password hash.

## Login

`POST /api/v1/auth/login`

Returns accessToken, refreshToken, and expiresIn. Access tokens are short-lived JWTs containing the user ID and role. Refresh tokens are opaque random values and are stored server-side only as SHA-256 hashes.

## Refresh

`POST /api/v1/auth/refresh`

Accepts the current refresh token and rotates it. A refresh token can be used once. Reuse revokes its token family and requires reauthentication.

## Authorization

Initial roles are `USER` and `ADMIN`. Non-public API requests require a valid access token. Role claims are mapped to Spring Security authorities.
