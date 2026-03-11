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
- trim leading/trailing spaces before save
- save `NULL` if trimmed value is empty
- preserve internal spaces (example: `" A B "` -> `"A B"`)
- insert into configured table in Oracle

## Add new file/table (no code change)
Add a new item under `app.loader.files`:

```yaml
app:
  loader:
    files:
      - file-name: NEWFILE.TXT
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

## Run
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postprod
```

or

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Scheduler
Cron is configurable in `src/main/resources/application.properties`:

```properties
app.scheduler.cron=0 0/5 * * * *
```
