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

### Methods

  * **run**

    ```sql
    procedure run(i_name varchar2)
    ```

    ```sql
    Run the extraction given by the `i_name` parameter.

    Arguments:
       [i_str] (varchar2): Name of the extraction in `ELO_TABLES`
    ```



  * **define**

    Inserts extraction definitions to `ELO_TABLES` and `ELO_COLUMNS`

    ```sql
      procedure script(
        i_table varchar2,
        i_dblk varchar2,
        i_name varchar2 default null,
        i_target_schema varchar2 default 'ODS'
      ) return varchar2
    ```

    ```sql
    Run the extraction given by the `i_name` parameter.

    Arguments:
       [i_table] (varchar2): Name of the source table.
       [i_dblk] (varchar2): db-link to use for source connection.
       [i_name] (varchar2): name of the extraction defaults to value of `i_table`
       [i_target_schema='ODS'] (varchar2): target schema name. where to load data.
    ```

  * **script**

    Returns a script for the extraction deployment.

    ```sql
      function script(
        i_table varchar2,
        i_dblk varchar2,
        i_name varchar2 default null,
        i_target_schema varchar2 default 'ODS'
      ) return varchar2
    ```

    ```sql
    Run the extraction given by the `i_name` parameter.

    Arguments:
       [i_table] (varchar2): Name of the source table.
       [i_dblk] (varchar2): db-link to use for source connection.
       [i_name] (varchar2): name of the extraction defaults to value of `i_table`
       [i_target_schema='ODS'] (varchar2): target schema name. where to load data.
    Returns
       (boolean): Returns a script for the extraction deployment
    ```