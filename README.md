<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.


Simple extract-load utility for **oracle** database. ELO is DB-Link only so you
can only use it for oracle -> oracle extractions. It is dead-simple and super fast
to define extraction rule for a table.

### Dependencies

  **ELO** uses [PL](https://github.com/bluecolor/pl) for logging.
  **PL** is a small utility and logging library for oracle.

### Installation

  * Make sure you have installed [PL](https://github.com/bluecolor/pl)

  * Change the current schema to util

    ```sql
    alter session set current_schema = util;
    ```

  * Run the contents of [init.sql](src/init.sql)

  * Run the contents of [elo.pks.sql](src/elo.pks.sql)

  * Run the contents of [elo.pkb.sql](src/elo.pkb.sql)

### Tables

  ##### ELO_TABLES

  * `name` unique name for the extraction of the table
  * `db_link` db link to use for the extraction
  * `source` source table including schema `eg. SRC_SCHEMA.TABLE_NAME`
  * `target` target table including schema `eg. TRG_SCHEMA.TABLE_NAME`
  * `filter` filter for the source data
  * `source_hint` select hint for the source
  * `target_hint` insert hint for the target
  * `delta_column` column to check if data is extracted using delta method.
  * `last_delta` last extracted value of the delta column
  * `excluded` exclude from running. usefull when you want to skip execution but keep the definition
  * `drop_create` load table with drop create like `create table table_name as select ...`

  ##### ELO_COLUMNS

  * `name` unique name for the extraction of the table. same name with `ELO_TABLES`
  * `source_col` source column or expression to extract.
  * `target_col` target column to load data.
  * `excluded` exclude from extracted columns. usefull when you want to skip the column but keep the definition


### Running

  Just call `elo.run` with a name parameter. example:

  ```sql
    elo.run('NAME_OF_EXT_DEF');
  ```

### Logs

  You can see the logs by issuing a select like;

  ```sql
    select * from util.logs order by 3 desc;
  ```


### Helpers

  Elo provides `define` helper mothod to quickly define extractions. You can also use `def` or `d` which are
  syntactic sugars over `define`.

  Example usages of `define`

  ```sql
    -- defines all columns of the table to column list
    begin
      util.elo.define('src_owner.src_table_name@my_db_link');
    end;
  ```

  ```sql
    -- only the specified columns
    begin
      util.elo.def(
        i_source  => 'src_owner.src_table_name@my_db_link',
        i_columns => 'first_col,second_col,another_col'
      );
    end;
  ```

  ```sql
    -- all options
    begin
      util.elo.d(
        i_name    => 'my_extraction',
        i_source  => 'src_owner.src_table_name',
        i_target  => 'trg_owner.trg_table_name',
        -- you can use new line and space in i_columns
        i_columns => 'first_col,second_col,another_col,
          yet_another_col, some_col_name
        ',
        i_db_link => 'my_db_link'
        i_filter  => 'my_column = 2'
      );
    end;
  ```

  ```sql
    -- Helper method to define extractions quickly. Inserts definitions to util.elo_tables and
    -- util.elo_columns.
    --
    -- If only i_source is given, it should be in the form of [remote_schema.remote_table@my_db_link]
    -- If i_columns is not given then all columns will be extracted
    -- If i_columns is given then new columns in the list will be inserted to the elo_columns.
    -- Definitions work in insert mode which means if table or column definitions already exists
    -- then only the new ones will be inserted. This method does not override any existing records in
    -- elo_tables or elo_columns
  ```

  There is also `add_filter` helper to add additional filtering to an existing extraction definition.

  Example;

  ```sql
  --- this will generate a query with a `where [existing_filter and] my_column = 2`
  begin
    util.elo.add_filter('my_extraction_name', 'my_column = 2');
  end;
  ```

  To delete entries from `elo_tables` or clear column mappings of an extraction use;
  - `delete`: Deletes an extraction definition including columns.
  - `del`: Same as `delete`
  - `delete_columns`: Deletes only column mappings for given extraction.
  - `delcols`: Same as `delete_columns`
