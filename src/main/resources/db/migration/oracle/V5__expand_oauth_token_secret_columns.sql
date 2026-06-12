ALTER TABLE chzzk_oauth_token MODIFY (
  access_token VARCHAR2(5000 CHAR),
  refresh_token VARCHAR2(5000 CHAR)
);
