# clfacv-loader

Spring Boot scheduler-based fixed-width file loader for Oracle.

## Tech stack
- Java 8
- Spring Boot 2.7.7
- Maven
- Spring JDBC + Oracle (`ojdbc8`)
- Lombok

## Active files and tables
Configured loaders (profile YAML):
- `CLFACV.TXT` -> `STG_HK_OBS_FACVDW`
- `CLFACVHASE.TXT` -> `STG_HK_OBS_FACVDW`
- `CLIMTM.TXT` -> `STG_HK_OBS_IMTM`

Column layouts are **not** read from YAML now.  
They are resolved from static registry:
- `src/main/java/com/clfacv/loader/config/FixedWidthLayoutRegistry.java`

## Loader flow (step by step)
1. Scheduler triggers from `app.loader.scheduler.cron` in active profile.
2. Loader validates required directories:
   - `input-directory`
   - `success-directory`
   - `failed-directory`
3. Loader reads configured files from `app.loader.files`.
4. For each file, loader resolves fixed-width layout from `FixedWidthLayoutRegistry` by file name (extension ignored).
5. Loader validates datasource key (`primary` / `secondary`) and layout columns.
6. Before file processing starts, loader deletes existing rows from each unique `datasource + table`.
7. For each configured file:
   - reads line by line
   - parses fixed-width values by layout lengths
   - trims leading/trailing spaces
   - stores `NULL` for empty trimmed values
   - inserts in batches (`batch-size`)
8. Insert behavior:
   - `STG_HK_OBS_FACVDW`: special insert with derived `TREE_ID`, `REGION`, and hardcoded `BATCH_RUN_ID = 1`
   - `STG_HK_OBS_IMTM`: insert with configured layout columns + `REGION` + hardcoded `BATCH_RUN_ID = 1`
9. Region mapping:
   - `CLFACVHASE*` -> `HKHASE`
   - all other files -> `HK`
10. After processing each file:
   - success -> move to `success-directory`
   - failure -> move to `failed-directory`
11. Moved filename format:
   - `<baseName>_yyyyMMdd_HHmmss<extension>`
   - example: `CLFACV_20260317_114500.TXT`

## Profile configuration
Set values in:
- `src/main/resources/application-postprod.yml`
- `src/main/resources/application-prod.yml`

Important keys:
- `spring.datasource.*` (primary datasource)
- optional `app.secondary-datasource.*` (secondary datasource)
- `app.loader.scheduler.cron`
- `app.loader.input-directory`
- `app.loader.success-directory`
- `app.loader.failed-directory`
- `app.loader.batch-size`
- `app.loader.files[]` (`file-name`, `data-source`, `table-name`)

## Run
Postprod:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postprod
```

Prod:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Add a new loader file
1. Add file entry in profile YAML (`file-name`, `data-source`, `table-name`).
2. Add corresponding static layout in `FixedWidthLayoutRegistry`.
3. If target table needs special insert behavior, extend `FixedWidthBatchRepository`.
