CREATE TABLE refresh_token_session (
  token_id VARCHAR2(64 CHAR) PRIMARY KEY,
  channel_id VARCHAR2(100 CHAR) NOT NULL,
  role VARCHAR2(30 CHAR) NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
  revoked_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_refresh_token_session_channel_id
  ON refresh_token_session(channel_id);
