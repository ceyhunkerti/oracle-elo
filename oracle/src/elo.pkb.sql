CREATE OR REPLACE PACKAGE BODY UTIL.ELO
AS


  GV_ERROR    constant varchar2(10) := 'ERROR';
  GV_SUCCESS  constant varchar2(10) := 'SUCCESS';
  GV_RUNNING  constant varchar2(10) := 'RUNNING';
  GV_READY    constant varchar2(10) := 'READY';

  gv_job_module   VARCHAR2(50)  := 'ELO';                 -- Job Module Name : Extract Load package
  gv_pck          VARCHAR2(50)  := 'ELO';                 -- PLSQL Package Name
  gv_job_owner    VARCHAR2(50)  := 'UTIL';                -- Owner of the Job
  gv_proc         VARCHAR2(100);                          -- Procedure Name
  gv_delim        VARCHAR2(10)  := ' : ';                 -- Delimiter Used In Logging

  gv_sql_errm     VARCHAR2(4000);                         -- SQL Error Message
  gv_sql_errc     NUMBER;                                 -- SQL Error Code
  gv_sql          LONG := '';
  gv_date_format  varchar2(30) := 'yyyy.mm.dd hh24:mi:ss';

  function build_sql(i_name varchar2) return clob;

  function fun_get_delta_col_type(
    i_db_link varchar2,
    i_table   varchar2,
    i_column  varchar2) return varchar2;

  procedure prc_update_last_delta(
    i_name            varchar2,
    i_table           varchar2,
    i_delta_col       varchar2,
    i_delta_col_type  varchar2
  );

  procedure set_status(i_name varchar2, i_status varchar2) as
    v_status varchar2(100);
    status_change_error exception;
    already_running_error exception;
    pragma exception_init(status_change_error, -20001);
    pragma exception_init(already_running_error, -20002);
  begin

	select t.status INTO v_status from util.elo_tables t where upper(t.name) = upper(i_name);

    if upper(i_status) = 'RUNNING' and v_status = 'RUNNING' then
      raise_application_error(-20002, i_name || ' is already running!');
    end if;
    update util.elo_tables set
      status = upper(i_status),
      start_time = decode(upper(i_status), 'RUNNING', sysdate, start_time),
      end_time = case when upper(i_status) in ('ERROR', 'SUCCESS') then sysdate else end_time end
    where upper(name) = upper(i_name);

    commit;
  end;

  function is_excluded(i_name varchar2) return boolean
  is
    v_count number := 0;
  begin
    select count(1) into v_count from util.elo_tables t
    where
      upper(t.name) = upper(i_name) and
      t.excluded = 1;
    return case v_count when 0 then false else true end;
  end;


  procedure reset is
  begin
    gv_proc := 'RUN';
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc);
    gv_sql := 'update util.elo_tables set
      start_time = null,
      end_time = null,
      status = upper('''|| GV_READY || ''')
    ';
    execute immediate gv_sql;
    pl.logger.success(SQL%ROWCOUNT || ' : updated', gv_sql);
    commit;
  exception
    when others then
      pl.logger.error(SQLCODE||' : '||SQLERRM, gv_sql);
      raise;
  end;


  procedure run(i_name varchar2, i_drop_create number default null, i_analyze number default null, i_index_drop number := 1)
  is
    v_db_link         varchar2(60);
    v_source          varchar2(100);
    v_target          varchar2(100);
    v_filter          varchar2(4000);
    v_source_hint     varchar2(4000);
    v_target_hint     varchar2(4000);
    v_analyze         number;
    v_drop_create     number;
    v_create_options  varchar2(4000);
    v_delta_column    varchar2(50);
    v_last_delta      varchar2(1000);
    v_source_cols     long;
    v_target_cols     long;
    v_delta_data_type varchar2(50);
    v_index_ddls      dbms_sql.varchar2_table;
    v_index_created   boolean := false;
  begin

    gv_proc := 'RUN';

    -- Initialize Log Variables
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc||'-'||i_name);

    if is_excluded(i_name) = true then
      pl.logger.warning(i_name || ' is EXCLUDED!');
      goto end_proc;
    end if;

    set_status(i_name, GV_RUNNING);

    pl.logger.info('Started extraction ...');

    select
      db_link, source, target, filter, source_hint, target_hint, v_delta_column, last_delta,
      drop_create, create_options, ANALYZE
    into
      v_db_link, v_source, v_target, v_filter, v_source_hint, v_target_hint, v_delta_column, v_last_delta,
      v_drop_create, v_create_options, v_analyze
    from util.ELO_TABLES where name = i_name;

    if trim(v_target_hint) is not null and instr(v_target_hint,'/*+') = 0
    then
      v_target_hint := '/*+ '||v_target_hint||' */';
    end if;

    if trim(v_source_hint) is not null and instr(v_source_hint,'/*+') = 0
    then
      v_source_hint := '/*+ '||v_source_hint||' */';
    end if;

    IF v_delta_column IS NOT NULL THEN
      v_delta_data_type := fun_get_delta_col_type(v_db_link, v_source, v_delta_column);
    END IF;

    if v_filter is not null or v_delta_column is not null then
      v_filter := 'WHERE ' || v_filter;
      if v_delta_column is not null then
        v_filter := v_filter || ' AND '||v_delta_column||'>'||
        case v_delta_data_type
          when 'DATE'     then pl.date_string(to_date(v_last_delta,gv_date_format))
          when 'NUMBER'   then to_number(v_last_delta)
          when 'CHAR'     then v_last_delta
          when 'VARCHAR'  then v_last_delta
          else v_last_delta
        end;
      end if;
    end if;

    begin
      for c in (select source_col, target_col from util.ELO_COLUMNS where excluded = 0 and name = i_name) loop
        v_source_cols := v_source_cols || c.source_col || ',';
        v_target_cols := v_target_cols || c.target_col || ',';
      end loop;

      v_source_cols := rtrim(v_source_cols, ',');
      v_target_cols := rtrim(v_target_cols, ',');
    exception when no_data_found then
      pl.logger.warning(i_name || ': All columns excluded or no elo def!');
      goto end_proc;
    end;

    if (i_drop_create is not null and i_drop_create != 0) or (i_drop_create is null and v_drop_create != 0) then

      pl.drop_table(v_target);

      gv_sql := '
        CREATE TABLE '|| v_target ||'
        '||v_create_options||'
        ('||v_target_cols|| ')
        AS
        SELECT '||v_source_hint||'
        '||v_source_cols||'
        FROM
          '||v_source||'@'||v_db_link||'
          '||v_filter||'';
    else
      gv_sql := '
        INSERT '||v_target_hint||' INTO '|| v_target ||'
        ('||v_target_cols|| ')
        SELECT '||v_source_hint||'
        '||v_source_cols||'
        FROM
          '||v_source||'@'||v_db_link||'
          '||v_filter||'';

      if v_delta_column is null then
        pl.logger.info('truncate table ' || v_target);
        execute immediate 'truncate table ' || v_target;
      end if;

    end if;


    if i_index_drop != 0 then
      v_index_ddls := pl.index_ddls(v_target);
      pl.drop_indexes(v_target);
    end if;

    execute immediate gv_sql;
    pl.logger.success(SQL%ROWCOUNT || ' : inserted', gv_sql);
    commit;

    if i_index_drop != 0 then
      pl.exec(v_index_ddls, true);
      v_index_created := true;
    end if;

    if v_delta_column is not null then
      prc_update_last_delta(
        i_name  => i_name,
        i_table => v_target,
        i_delta_col => v_delta_column,
        i_delta_col_type => v_delta_data_type
      );
    end if;

    if (i_analyze is not null and i_analyze != 0) or (v_analyze != 0)  then
      pl.gather_table_stats(i_table => v_target);
    end if;

    <<end_proc>>
    set_status(i_name, GV_SUCCESS);
  exception
    when others then
      pl.logger.error(SQLCODE||' : '||SQLERRM, gv_sql);
      set_status(i_name, GV_ERROR);
      if i_index_drop != 0 and v_index_created = false then
        pl.exec(v_index_ddls, true);
      end if;
      raise;
  end;

  function fun_get_delta_col_type(i_db_link varchar2, i_table varchar2, i_column varchar2) return varchar2
  is
    v_owner       varchar2(30) := substr(i_table,1,instr(i_table, '.')-1);
    v_table_name  varchar2(30) := substr(i_table,instr(i_table, '.')+1);
    v_col_type    varchar2(50);
  begin

    gv_sql:= '
      select data_type from all_tab_cols@'||i_db_link||'
      where owner = '''||v_owner||''' and table_name='''||v_table_name||''' and column_name = '''|| i_column||'''
    ';

    execute immediate gv_sql into v_col_type;

    return gv_sql;

  end;

  procedure prc_update_last_delta(
    i_name            varchar2,
    i_table           varchar2,
    i_delta_col       varchar2,
    i_delta_col_type  varchar2
  )
  is
    v_last_delta varchar2(1000);
  begin

    if i_delta_col_type = 'DATE' then
      gv_sql := 'to_char(max('||i_delta_col||'),'''||gv_date_format||''')';
    else
      gv_sql := 'max('||i_delta_col||')';
    end if;

    gv_sql := 'select /*+ parallel(16) */ '||gv_sql||' from '||i_table;

    execute immediate gv_sql into v_last_delta;

    gv_sql := '
      update util.ELO_TABLES set last_delta = '''||v_last_delta||'''
      where name = '''||i_name||'''
    ';

    execute immediate gv_sql;

    commit;
  end;


  -- Args:
  --    [i_source varchar2]: 'owner.source_table@dblink'
  --    [i_create boolean := false]: create target table
  procedure def(i_source varchar2, i_create boolean := false) is
    v_tokens dbms_sql.varchar2_table;
  begin
    v_tokens := pl.split(i_source, '@');
    def(
      i_source  => v_tokens(1),
      i_dblk    => v_tokens(2),
      i_create  => i_create
    );
  end;

  -- sugar for define
  procedure def(
    i_source varchar2,
    i_dblk  varchar2,
    i_elo_name  varchar2 default null,
    i_target_schema varchar2 default 'SG',
    i_create  boolean default false
  ) as
  begin
    define(
      i_source => i_source,
      i_dblk  => i_dblk,
      i_elo_name => i_elo_name,
      i_target_schema => i_target_schema,
      i_create => i_create
    );
  end;

  procedure define(
    i_source varchar2,
    i_dblk  varchar2,
    i_elo_name  varchar2 default null,
    i_target_schema varchar2 default 'SG',
    i_create  boolean default false
  )
  is
    table_is_null     exception;
    db_link_is_null   exception;

    pragma exception_init(table_is_null,   -20170);
    pragma exception_init(db_link_is_null, -20171);

    v_script  long := '';
    v_columns long := '';

    type source_cursor_type is ref cursor;
    c source_cursor_type;

    v_column_name  varchar2(100);
    v_data_type    varchar2(100);
    v_data_length  number;
  begin

    gv_proc := 'DEFINE';

    -- Initialize Log Variables
    pl.logger := util.logtype.init(gv_pck || '.' || gv_proc);

    if i_source is null then raise table_is_null; end if;

    if i_target_schema is null then raise db_link_is_null; end if;

    gv_sql := 'select column_name, data_type, data_length from all_tab_cols@'||i_dblk|| '
      where owner||''.''||table_name = '''||upper(i_source)||''' and
      hidden_column = ''NO''
    ';

    if i_create = true then
      pl.drop_table(i_target_schema||'.'||substr(i_source, 1 + instr(i_source,'.')));
      v_script := '
        create table '||i_target_schema||'.'||substr(i_source, 1 + instr(i_source,'.'))||'
        (
          $COLUMNS
        )
      ';

      open c for gv_sql;
      loop
        fetch c into v_column_name, v_data_type, v_data_length;
        exit when c%notfound;
        if v_data_type in ('CHAR','VARCHAR','VARCHAR2','NUMBER', 'RAW') then
          v_columns := v_columns || v_column_name||' '||v_data_type||'('||v_data_length||'),'||chr(10);
        else
          v_columns := v_columns || v_column_name||' '||v_data_type||','||chr(10);
        end if;
      end loop;

      v_columns := rtrim(v_columns, ','||chr(10));
      v_script := replace(v_script,'$COLUMNS',v_columns) ||chr(10)||chr(10);

      execute immediate v_script;
      pl.logger.success('Table created', v_script);
    end if;

    -- define extraction table
    v_script := 'INSERT INTO util.ELO_TABLES (
      NAME,
      DB_LINK,
      SOURCE,
      TARGET
    ) VALUES (
      '''||nvl(i_elo_name, i_source)||''',
      '''||i_dblk||''',
      '''||i_source||''',
      '''||i_target_schema||'.'||substr(i_source, 1 + instr(i_source,'.'))||'''
    )';

    execute immediate v_script;
    pl.logger.success('Table defined in elo_tables', v_script);


    -- define extraction rules
    open c for gv_sql;
    loop
      fetch c into v_column_name, v_data_type, v_data_length;
      exit when c%notfound;

      v_script := 'INSERT INTO util.ELO_COLUMNS (
        NAME,
        SOURCE_COL,
        TARGET_COL
      ) VALUES (
        '''||nvl(i_elo_name, i_source)||''',
        '''||v_column_name||''',
        '''||v_column_name||'''
      )';

      execute immediate v_script;
      pl.logger.success('Column defined in elo_columns', v_script);
    end loop;

    commit;

  exception
    when others then
      gv_sql_errc := SQLCODE;
      gv_sql_errm := SQLERRM;
      pl.logger.error(gv_sql_errc||' : '||gv_sql_errm, gv_sql);
      raise;
  end;

  function script(
    i_table varchar2,
    i_dblk  varchar2,
    i_name  varchar2 default null,
    i_target_schema varchar2 default 'SG'
  ) return varchar2
  is
    table_is_null     exception;
    db_link_is_null   exception;

    pragma exception_init(table_is_null,   -20170);
    pragma exception_init(db_link_is_null, -20171);

    v_script  long := '';
    v_columns long := '';

    type source_cursor_type is ref cursor;
    c source_cursor_type;

     v_column_name  varchar2(100);
     v_data_type    varchar2(100);
     v_data_length  number;
  begin

    if i_table is null then raise table_is_null; end if;

    if i_target_schema is null then raise db_link_is_null; end if;


    v_script := '
      create table '||i_target_schema||'.'||substr(i_table, instr(i_target_schema,'.'))||'
      (
        $COLUMNS
      );
    ';

    gv_sql := 'select column_name, data_type, data_length from all_tab_cols@'||i_dblk|| '
      where owner||''.''||table_name = '''||upper(i_table)||''' and
      hidden_column = ''NO''
    ';
    open c for gv_sql;

    loop

      fetch c into v_column_name, v_data_type, v_data_length;
      exit when c%notfound;

      if v_data_type in ('CHAR','VARCHAR','VARCHAR2','NUMBER', 'RAW') then
        v_columns := v_columns || v_column_name||' '||v_data_type||'('||v_data_length||'),'||chr(10);
      else
        v_columns := v_columns || v_column_name||' '||v_data_type||','||chr(10);
      end if;
    end loop;

    v_columns := rtrim(v_columns, ','||chr(10));

    v_script := replace(v_script,'$COLUMNS',v_columns) ||chr(10)||chr(10);

    v_script := v_script || 'INSERT INTO util.ELO_TABLES (
      NAME,
      DB_LINK,
      SOURCE,
      TARGET
    ) VALUES (
      '''||nvl(i_name,i_table)||''',
      '''||i_dblk||''',
      '''||i_table||''',
      '''||i_target_schema||'.'||substr(i_table, instr(i_target_schema,'.'))||'''
    );'||chr(10)||chr(10);


    open c for gv_sql;

    loop

      fetch c into v_column_name, v_data_type, v_data_length;
      exit when c%notfound;

      v_script := v_script || 'INSERT INTO util.ELO_COLUMNS (
        NAME,
        SOURCE_COL,
        TARGET_COL
      ) VALUES (
        '''||nvl(i_name, i_table)||''',
        '''||v_column_name||''',
        '''||v_column_name||'''
      );'||chr(10)||chr(10);

    end loop;

    v_script := v_script || ' commit;';

    return v_script;

  end;

  function build_sql(i_name varchar2) return clob
  is
    v_db_link         varchar2(60);
    v_source          varchar2(100);
    v_target          varchar2(100);
    v_filter          varchar2(4000);
    v_source_hint     varchar2(4000);
    v_target_hint     varchar2(4000);
    v_delta_column    varchar2(50);
    v_last_delta      varchar2(1000);
    v_source_cols     long;
    v_target_cols     long;
    v_delta_data_type varchar2(50);
  begin

    select
      db_link, source, target, filter, source_hint, target_hint, v_delta_column, last_delta
    into
      v_db_link, v_source, v_target, v_filter, v_source_hint, v_target_hint, v_delta_column, v_last_delta
    from
      util.ELO_TABLES
    where
      name = i_name;

    if trim(v_target_hint) is not null and instr(v_target_hint,'/*+') = 0
    then
      v_target_hint := '/*+ '||v_target_hint||' */';
    end if;

    if trim(v_source_hint) is not null and instr(v_source_hint,'/*+') = 0
    then
      v_source_hint := '/*+ '||v_source_hint||' */';
    end if;

    IF v_delta_column IS NOT NULL THEN
      v_delta_data_type := fun_get_delta_col_type(v_db_link, v_source, v_delta_column);
    END IF;

    if v_filter is not null or v_delta_column is not null then
      v_filter := 'WHERE ' || v_filter;
      if v_delta_column is not null then
        v_filter := v_filter || ' AND '||v_delta_column||'>'||
        case v_delta_data_type
          when 'DATE'     then pl.date_string(to_date(v_last_delta,gv_date_format))
          when 'NUMBER'   then to_number(v_last_delta)
          when 'CHAR'     then v_last_delta
          when 'VARCHAR'  then v_last_delta
          else v_last_delta
        end;
      end if;
    end if;


    for c in (select source_col, target_col from util.ELO_COLUMNS where name = i_name) loop
       v_source_cols := v_source_cols || c.source_col || ',';
       v_target_cols := v_target_cols || c.target_col || ',';
    end loop;

    v_source_cols := rtrim(v_source_cols, ',');
    v_target_cols := rtrim(v_target_cols, ',');

    gv_sql := '
      INSERT '||v_target_hint||' INTO '|| v_target ||'
      ('||v_target_cols|| ')
      SELECT '||v_source_hint||'
      '||v_source_cols||'
      FROM
        '||v_source||'@'||v_db_link||'
      '||v_filter||'';

    return gv_sql;

  end;

  function simulate(i_name varchar2) return clob
  is
  begin
    return build_sql(i_name);
  end;

END;