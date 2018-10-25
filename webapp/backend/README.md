# Alchemist - Oracle Data Integrator Magic


### Getting started with minimal configuration
- un-compress the archive file
- `cd backend`
- `gradle bootrun`
- create system user from the `alchemist shell`:
  ```
    ssh user@localhost -p 2000
    # enter password
    su -h # help for su command
    # example
    # su -m c -u system -p system -n system -e system@your.domain.com
    # username: system password: system
  ```
- open app in browser and login with `system user` that you created in previous step.

