CREATE TABLE SC_Blog_Post
(
  pubId      INT         NOT NULL,
  dateEvent  CHAR(13)    NOT NULL DEFAULT ('0000000000000'),
  instanceId VARCHAR(50) NOT NULL
);

ALTER TABLE SC_Blog_Post ADD
CONSTRAINT PK_Blog_Post PRIMARY KEY
  (
    pubId
  );