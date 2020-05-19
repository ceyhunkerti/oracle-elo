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
    v_name            varchar2(100) := upper(i_name);
  begin

    gv_proc := 'RUN';

    -- Initialize Log Variables
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc||'-'||v_name);

    if is_excluded(v_name) = true then
      pl.logger.warning(v_name || ' is EXCLUDED!');
      goto end_proc;
    end if;

    set_status(v_name, GV_RUNNING);

    pl.logger.info('Started extraction ...');

    select
      db_link, source, target, filter, source_hint, target_hint, v_delta_column, last_delta,
      drop_create, create_options, ANALYZE
    into
      v_db_link, v_source, v_target, v_filter, v_source_hint, v_target_hint, v_delta_column, v_last_delta,
      v_drop_create, v_create_options, v_analyze
    from util.ELO_TABLES where name = v_name;

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
      pl.logger.warning(v_name || ': All columns excluded or no elo def!');
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
        i_name  => v_name,
        i_table => v_target,
        i_delta_col => v_delta_column,
        i_delta_col_type => v_delta_data_type
      );
    end if;

    if (i_analyze is not null and i_analyze != 0) or (v_analyze != 0)  then
      pl.gather_table_stats(i_table => v_target);
    end if;

    <<end_proc>>
    set_status(v_name, GV_SUCCESS);
  exception
    when others then
      pl.logger.error(SQLCODE||' : '||SQLERRM, gv_sql);
      set_status(v_name, GV_ERROR);
      if i_index_drop != 0 and v_index_created = false then
        pl.exec(v_index_ddls, true);
      end if;
      rollback;
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

  -- private
  function get_remote_col_list(i_table varchar2, i_db_link varchar2) return dbms_sql.varchar2_table is
    type source_cursor_type is ref cursor;
    c source_cursor_type;
    v_column_name varchar2(128);
    v_result dbms_sql.varchar2_table;
    v_index number := 1;
    v_table varchar2(128) := i_table;
  begin

    if instr(v_table, '"') = 0 then v_table := upper(v_table); end if;

    gv_sql := '
      select column_name from all_tab_cols@'||i_db_link||'
      where
        '''||v_table||''' = owner||''.''||table_name
        and data_type not in (''CLOB'', ''LOB'') --! todo: add others
        and hidden_column = ''NO''
      order by column_id
    ';

    open c for gv_sql;
    loop
      fetch c into v_column_name; exit when c%notfound;
      v_result(v_index) := v_column_name;
      v_index := v_index + 1;
    end loop;

    return v_result;
  exception
    when others then
      pl.logger.error(gv_sql);
      raise;
  end;

  function is_in_elo_tables(i_name varchar2) return boolean is
    v_result number;
    v_name   varchar2(128) := i_name;
  begin
    if instr(i_name, '"') = 0 then
      v_name := upper(v_name);
    end if;

    select count(1) into v_result from util.elo_tables t
    where t.name = v_name;

    return case v_result when 1 then true else false end;
  end;

  function is_in_elo_columns(i_name varchar2, i_column varchar2) return boolean is
    v_result number;
    v_name   varchar2(128) := i_name;
    v_column varchar2(128) := i_column;
  begin
    if instr(i_name, '"') = 0 then
      v_name := upper(v_name);
    end if;

    if instr(i_column, '"') = 0 then
      v_column := upper(v_column);
    end if;

    select count(1) into v_result from util.elo_columns t
    where t.name = v_name and t.target_col = v_column;

    return case v_result when 1 then true else false end;
  end;

  -- sugar for elo.define
  procedure d(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null,
    i_source_hint varchar2 := null,
    i_target_hint varchar2 := null
  ) is
  begin
    define(i_source, i_name, i_target, i_db_link, i_columns, i_filter, i_source_hint, i_target_hint);
  end;

  -- sugar for elo.define
  procedure def(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null,
    i_source_hint varchar2 := null,
    i_target_hint varchar2 := null
  ) is
  begin
    define(i_source, i_name, i_target, i_db_link, i_columns, i_filter, i_source_hint, i_target_hint);
  end;



  -- Helper method to define extractions quickly. Inserts definitions to util.elo_tables and
  -- util.elo_columns.
  --
  -- If only i_source is given, it should be in the form of [remote_schema.remote_table@my_db_link]
  -- If i_columns is not given then all columns will be extracted
  -- If i_columns is given then new columns in the list will be inserted to the elo_columns.
  -- Definitions work in insert mode which means if table or column definitions already exists
  -- then only the new ones will be inserted. This method does not override any existing records in
  -- elo_tables or elo_columns
  procedure define(
    i_source  varchar2,
    i_name    varchar2 := null,
    i_target  varchar2 := null,
    i_db_link varchar2 := null,
    i_columns long := null,
    i_filter  varchar2 := null,
    i_source_hint varchar2 := null,
    i_target_hint varchar2 := null
  ) is
    e_source_parameter_error  exception;
    pragma exception_init(e_source_parameter_error,  -20170);
    v_tokens dbms_sql.varchar2_table;
    v_token  varchar2(128);
  begin
    gv_proc := 'define';
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc||'-'||i_source);

    if i_source is null then
      raise_application_error(-20170, 'source parameter can not be null');
    end if;

    if substr(i_source, -1) = ' ' or substr(i_source, 1, 1) = ' ' then
      raise_application_error(-20170, 'parameters can not begin or end with a space');
    end if;

    if instr(i_source, '@') > 0 then
      v_tokens := pl.split(i_source, '@');
      define(
        i_source  => v_tokens(1),
        i_name    => i_name,
        i_target  => i_target,
        i_db_link => v_tokens(2),
        i_columns => i_columns,
        i_filter  => i_filter,
        i_source_hint => i_source_hint,
        i_target_hint => i_target_hint
      );
      return;
    end if;

    if i_target is null then
      define(
        i_source  => i_source,
        i_name    => i_name,
        i_target  => i_source,
        i_db_link => i_db_link,
        i_columns => i_columns,
        i_filter  => i_filter,
        i_source_hint => i_source_hint,
        i_target_hint => i_target_hint
      );
      return;
    end if;

    if i_name is null then
      v_tokens := pl.split(i_source, '.');
      define(
        i_source  => i_source,
        i_name    => v_tokens(2),
        i_target  => i_source,
        i_db_link => i_db_link,
        i_columns => i_columns,
        i_filter  => i_filter,
        i_source_hint => i_source_hint,
        i_target_hint => i_target_hint
      );
      return;
    end if;

    if i_columns is null then
      define(
        i_source  => i_source,
        i_name    => i_name,
        i_target  => i_source,
        i_db_link => i_db_link,
        i_columns => pl.make_string(get_remote_col_list(i_source, i_db_link)),
        i_filter  => i_filter,
        i_source_hint => i_source_hint,
        i_target_hint => i_target_hint
      );
      return;
    end if;

    if is_in_elo_tables(i_name) = false then
      gv_sql := '
        insert into util.elo_tables (
          name, db_link, source, target, filter, drop_create,
          source_hint, target_hint
        ) values (
          '''||i_name||''', '''||i_db_link||''', '''||i_source||''', '''||i_target||''', '''||i_filter||''', 1,
          '''||i_source_hint||''', '''||i_target_hint||'''
        )
      ';
      execute immediate gv_sql;
    end if;

    v_tokens := pl.split(i_columns);
    for i in v_tokens.first .. v_tokens.last loop

      v_token := trim(translate(v_tokens(i), chr(10)||chr(13), ' '));
      if instr(v_token, '"') = 0 then v_token := upper(v_token); end if;

      if is_in_elo_columns(i_name, v_token) = false then
        gv_sql := '
          insert into util.elo_columns (
            name, source_col, target_col
          ) values (
            '''||i_name ||''',
            '''||v_token||''',
            '''||v_token||'''
          )
        ';
        execute immediate gv_sql;
      end if;
    end loop;

    commit;

  exception
    when others then
      pl.logger.error(gv_sql);
      rollback;
      raise;
  end;


  procedure add_filter(i_name varchar2, i_filter varchar2) is
  begin
    if i_name is null then return; end if;
    gv_proc   := 'add_filter';
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc);

    gv_sql := '
      update util.elo_tables
      set filter =
        case when filter is null then '''||i_filter||''' else '''||' and '||i_filter||''' end
      where
        name = '''||i_name||'''
    ';

    execute immediate gv_sql;
    pl.logger.success(gv_sql);
    commit;

  exception
    when others then
      pl.logger.error(gv_sql);
      rollback;
      raise;
  end;

  -- sugar for delete
  procedure del(i_name varchar2) is
  begin
    elo.delete(i_name);
  end;

  -- delete a mapping by name
  procedure delete(i_name varchar2) is
  begin
    gv_proc   := 'delete';
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc);

    gv_sql := 'delete from util.elo_columns where name='''||i_name||'''';
    pl.logger.success(SQL%ROWCOUNT || ' : deleted', gv_sql);
    execute immediate gv_sql;

    gv_sql := 'delete from util.elo_tables where name='''||i_name||'''';
    pl.logger.success(SQL%ROWCOUNT || ' : deleted', gv_sql);
    execute immediate gv_sql;

    commit;

  exception
    when others then
      pl.logger.error(gv_sql);
      rollback;
      raise;
  end;


  -- sugar for delete_columns
  procedure delcols(i_name varchar2) is
  begin
    delete_columns(i_name);
  end;


  procedure delete_columns(i_name varchar2) is
  begin

    gv_proc   := 'delete_columns';
    pl.logger := util.logtype.init(gv_pck||'.'||gv_proc);

    gv_sql := 'delete from util.elo_columns where name='''||i_name||'''';
    pl.logger.success(SQL%ROWCOUNT || ' : deleted', gv_sql);
    execute immediate gv_sql;

    commit;

  exception
    when others then
      pl.logger.error(gv_sql);
      rollback;
      raise;
  end;


END;