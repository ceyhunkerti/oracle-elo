<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.


Simple extract/load platform for oracle to oracle environments.

Application consists of two parts;
- PLSQL package that does the actual job
- Frontend application for easily managind extraction definitions

Front end application is optional. You can use ELO by just using the PLSQL package.

## DEPENDENCIES

  - **ELO** uses [PL](https://github.com/bluecolor/pl) for logging.
  **PL** is a small utility and logging library for oracle.


## INSTALLATION

### Oracle Side

  * Make sure you have installed [PL](https://github.com/bluecolor/pl)

  * Change the current schema to util

    ```sql
    alter session set current_schema = util;
    ```

  * Run the contents of [init.sql](oracle/src/init.sql)

  * Run the contents of [elo.pks.sql](oracle/src/elo.pks.sql)

  * Run the contents of [elo.pkb.sql](oracle/src/elo.pkb.sql)

  * For details see [README](oracle) for oracle side

### Web Application

  * Download the latest relase from [here](dist)

  * Run it with `java -jar elo.jar`








