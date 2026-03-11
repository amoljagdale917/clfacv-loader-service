# clfacv-loader

Spring Boot scheduler-based fixed-width file loader.

## Tech stack
- Java 8
- Spring Boot 2.7.7
- Maven
- Spring JDBC + Oracle (`ojdbc8`)

## Current setup
Current config loads:
- `lms/file/input/CLFACV.TXT`
- `lms/file/input/CLFACVHASE.TXT`

Both are mapped to table `STG_HK_OBS_FACV` using lengths:
`3,12,3,1,1,11,11,5,11,7,1`

## Reusable loader logic
Loader is now config-driven using `app.loader.files` in:
- `src/main/resources/application.yml`

For each configured file:
- parse fixed-width columns by configured lengths
- use configured datasource (`primary` or `secondary`)
- trim leading/trailing spaces before save
- save `NULL` if trimmed value is empty
- preserve internal spaces (example: `" A B "` -> `"A B"`)
- insert into configured table in Oracle
- if load succeeds, move file to `success-directory`
- if load fails, move file to `failed-directory`
- moved name format: `fileName_yyyyMMdd_HHmmss`

## Add new file/table (no code change)
Add a new item under `app.loader.files`:

```yaml
app:
  loader:
    files:
      - file-name: NEWFILE.TXT
        data-source: primary
        table-name: STG_NEW_TABLE
        columns:
          - name: COL1
            length: 10
          - name: COL2
            length: 5
```

## Configure DB profiles
Set datasource values in:
- `src/main/resources/application-postprod.yml`
- `src/main/resources/application-prod.yml`

Primary datasource:
- configured with `spring.datasource.*`

Optional secondary datasource:
- configure `app.secondary-datasource.*`
- when not configured, only `primary` is usable

Example secondary datasource config:
```yaml
app:
  secondary-datasource:
    url: jdbc:oracle:thin:@//host:1521/SERVICE
    username: user2
    password: pass2
    driver-class-name: oracle.jdbc.OracleDriver
```

Use required datasource per file:
```yaml
app:
  loader:
    files:
      - file-name: FILE_A.TXT
        data-source: primary
        table-name: TABLE_A
        columns: [...]
      - file-name: FILE_B.TXT
        data-source: secondary
        table-name: TABLE_B
        columns: [...]
```

## Profile paths
Postprod:
- input: `/var/hub/incomong`
- success: `/var/file/success`
- failed: `/var/file/failed`

Prod:
- input: `lms/files/input`
- success: `lms/files/success`
- failed: `lms/files/failed`

## Run
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postprod
```

or

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Scheduler
Cron is profile-specific:
- `src/main/resources/application-prod.yml`: `0 0 0 * * *` (midnight daily)
- `src/main/resources/application-postprod.yml`: `0 0/5 * * * *` (test default, change as needed)

If profile cron is not set, code fallback is:

```properties
app.scheduler.cron=0 0/5 * * * *
```
