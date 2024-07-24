CREATE TABLE IF NOT EXISTS tbl_vacation
(
    id                          BIGSERIAL NOT NULL PRIMARY KEY,
    created_on                  TIMESTAMPTZ NOT NULL,
    created_by                  VARCHAR(100) NOT NULL,
    modified_on                 TIMESTAMPTZ NOT NULL,
    modified_by                 VARCHAR(100) NOT NULL,

    author                      BIGINT NOT NULL,
    status                      VARCHAR(20) NOT NULL,
    resolved_by                 BIGINT NULL,
    request_date                TIMESTAMPTZ NOT NULL,
    start_date                  TIMESTAMPTZ NOT NULL,
    end_date                    TIMESTAMPTZ NOT NULL
);
