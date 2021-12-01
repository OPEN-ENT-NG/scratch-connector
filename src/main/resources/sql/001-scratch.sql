CREATE SCHEMA scratch;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE scratch.scripts (
    filename character varying(255) NOT NULL,
    passed timestamp with time zone,
    CONSTRAINT scripts_pkey PRIMARY KEY (filename)
);

CREATE TABLE scratch.sessions (
      id bigserial NOT NULL,
      fileid character varying(36),
      entid character varying(36),
      documentname text,
      created timestamp with time zone NOT NULL DEFAULT now(),
      userid character varying(36) NOT NULL,
      sessionid character varying(255),
      canupdate boolean NOT NULL DEFAULT FALSE,
      CONSTRAINT sessions_pkey PRIMARY KEY (id)
);