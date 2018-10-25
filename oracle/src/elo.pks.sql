CREATE OR REPLACE PACKAGE UTIL.ELO AUTHID CURRENT_USER
AS

  type varchar2_table is table of varchar2(4000);

  procedure reset;

  procedure run(
    i_name varchar2,
    i_drop_create number default null,
    i_analyze number default null
  );

  procedure def(
    i_source        varchar2,
    i_dblk          varchar2,
    i_elo_name      varchar2 default null,
    i_target_schema varchar2 default 'ODS',
    i_create        boolean default false
  );

  procedure define(
    i_source         varchar2,
    i_dblk          varchar2,
    i_elo_name      varchar2 default null,
    i_target_schema varchar2 default 'ODS',
    i_create        boolean default false
  );

  function script(
    i_table         varchar2,
    i_dblk          varchar2,
    i_name          varchar2 default null,
    i_target_schema varchar2 default 'ODS'
  ) return varchar2;

  function simulate(i_name varchar2) return clob;

END;