# clfacv-loader

Spring Boot scheduler-based loader for fixed-width FACV files:
- `lms/file/input/CLFACV.TXT`
- `lms/file/input/CLFACVHASE.TXT`

## Tech stack
- Java 8
- Spring Boot 2.7.7
- Maven
- Spring JDBC + Oracle (`ojdbc8`)

## STG_HK_OBS_FACV mapping (fixed-width)
| Column | Length |
|---|---:|
| BNK_NO | 3 |
| CUST_ACCT_NO | 12 |
| SYS_COD | 3 |
| REC_TYPE | 1 |
| CUST_GP | 1 |
| ITL_CUST_NO | 11 |
| FILLER | 11 |
| LMT_ID | 5 |
| CUST_ID | 11 |
| FILLER1 | 7 |
| MAINT_ACT | 1 |

All fields are:
- trimmed before save (leading and trailing spaces removed)
- saved as `NULL` when value becomes empty after trim
- internal spaces are preserved (example: `" A B " -> "A B"`)

## Configure DB
Edit `src/main/resources/application.properties`:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## Run
```bash
mvn spring-boot:run
```

Scheduler cron is configurable via:
```properties
app.scheduler.cron=0 0/5 * * * *
```

## Optional table DDL (Oracle)
```sql
CREATE TABLE STG_HK_OBS_FACV (
  BNK_NO       CHAR(3),
  CUST_ACCT_NO CHAR(12),
  SYS_COD      CHAR(3),
  REC_TYPE     CHAR(1),
  CUST_GP      CHAR(1),
  ITL_CUST_NO  CHAR(11),
  FILLER       CHAR(11),
  LMT_ID       CHAR(5),
  CUST_ID      CHAR(11),
  FILLER1      CHAR(7),
  MAINT_ACT    CHAR(1)
);
```
