drop table elo_tables;

CREATE TABLE ELO_TABLES
(
  name            varchar2(100) primary key,
  db_link         varchar2(60),
  source          varchar2(100),
  target          varchar2(100),
  filter          varchar2(4000),
  source_hint     varchar2(4000),
  target_hint     varchar2(4000),
  delta_column    varchar2(50),
  last_delta      varchar2(1000),
  excluded        number default 0,
  drop_create     number default 0,
  create_options  varchar2(4000) default null,
  status          varchar2(100) default 'READY',
  analyze         number default 0,
  start_time      date,
  end_time        date
)
NOLOGGING;



CREATE OR REPLACE TRIGGER TRG_ELO_TABLES_UCASE
BEFORE INSERT ON ELO_TABLES FOR EACH ROW
DECLARE
BEGIN
  :new.name    := upper(:new.name);
  :new.db_link := upper(:new.db_link);
  :new.source  := :new.source;
  :new.target  := upper(:new.target);
END;
/


drop table ELO_COLUMNS;

CREATE TABLE ELO_COLUMNS
(
  name          varchar2(100),
  source_col    varchar2(1000),
  target_col    varchar2(50),
  excluded      number default 0,
  constraint PK_ELO_COLUMNS primary key (name, target_col)
)
NOLOGGING;



CREATE OR REPLACE TRIGGER TRG_ELO_COLUMNS_UCASE
BEFORE INSERT ON ELO_COLUMNS FOR EACH ROW
DECLARE
BEGIN
  :new.name       := upper(:new.name);
  :new.source_col := :new.source_col;
  :new.target_col := upper(:new.target_col);
END;
/