CREATE OR REPLACE PACKAGE UTIL.ELO AUTHID CURRENT_USER
AS

  type varchar2_table is table of varchar2(4000);

  procedure reset;

  procedure run(
    i_name varchar2,
    i_drop_create number default null,
    i_analyze number default null,
    i_index_drop number := 1
  );

  procedure define(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null
  );
  procedure def(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null
  );
  procedure d(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null
  );

  procedure add_filter(i_name varchar2, i_filter varchar2);

END;