[![Project Status: Work in Progress](https://img.shields.io/badge/Project%20Status-Work%20in%20Progress-orange.svg)](https://img.shields.io/badge/Project%20Status-Work%20in%20Progress-orange.svg)

# Billing App

A batch application that generates billing reports for an imaginary cellphone company called Spring Cellular. The application stores billing information in a relational database and generates a billing report. This application is based on Spring Boot and uses Spring Batch's features to create a robust batch-processing system that is restartable and fault-tolerant.

## Postgres database container

Run the container locally on port 5000
```
docker run --name postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5000:5432 -d postgres
```

Connect to the database
```
docker exec -it postgres psql -U postgres
```

Create tables
```sql
CREATE TABLE BATCH_JOB_INSTANCE  (
    JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT ,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;
CREATE TABLE BATCH_JOB_EXECUTION  (
    JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT  ,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL ,
    END_TIME TIMESTAMP DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED TIMESTAMP,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
    references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
    JOB_EXECUTION_ID BIGINT NOT NULL ,
    PARAMETER_NAME VARCHAR(100) NOT NULL ,
    PARAMETER_TYPE VARCHAR(100) NOT NULL ,
    PARAMETER_VALUE VARCHAR(2500) ,
    IDENTIFYING CHAR(1) NOT NULL ,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;
CREATE TABLE BATCH_STEP_EXECUTION  (
    STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP DEFAULT NULL ,
    END_TIME TIMESTAMP DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    COMMIT_COUNT BIGINT ,
    READ_COUNT BIGINT ,
    FILTER_COUNT BIGINT ,
    WRITE_COUNT BIGINT ,
    READ_SKIP_COUNT BIGINT ,
    WRITE_SKIP_COUNT BIGINT ,
    PROCESS_SKIP_COUNT BIGINT ,
    ROLLBACK_COUNT BIGINT ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED TIMESTAMP,
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
    references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;
CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
    references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;
create table BILLING_DATA
(
    DATA_YEAR     INTEGER,
    DATA_MONTH    INTEGER,
    ACCOUNT_ID    INTEGER,
    PHONE_NUMBER  VARCHAR(12),
    DATA_USAGE    FLOAT,
    CALL_DURATION INTEGER,
    SMS_COUNT     INTEGER
);
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ MAXVALUE 9223372036854775807 NO CYCLE;
```

Check tables
```
postgres=# \d
```

Verify job status
```
select * from BATCH_JOB_EXECUTION;
```

Launch the Job and pass the input file as a parameter
```
java -jar target/billing-app-0.0.1-SNAPSHOT.jar input.file=src/main/resources/billing-2024-01.csv
```

Useful commands (If you want use Docker, add 'docker exec postgres psql -U postgres -c' command at the beginning of each command)
```sql
select * from BATCH_JOB_INSTANCE;
```

```sql
select * from BATCH_JOB_EXECUTION;
```

```sql
select * from BATCH_JOB_EXECUTION_PARAMS;
```

```sql
select count(*) from BATCH_JOB_EXECUTION;
```

```
mvn clean package -D maven.test.skip=true 
```

```
java -jar target/billing-app-0.0.1-SNAPSHOT.jar input.file=src/main/resources/billing-01.csv 
```

Run the job with all the steps
```
java -jar target/billing-app-0.0.1-SNAPSHOT.jar input.file=input/billing-01.csv output.file=staging/billing-report-2023-01.csv data.year=2023 data.month=1
```

Run the failing job
```
java -jar target/billing-app-0.0.1-SNAPSHOT.jar input.file=input/billing-2023-03.csv output.file=staging/billing-report-2023-03.csv data.year=2023 data.month=3
```

Inspect the metadata of job executions
```sql
select job_instance_id, job_execution_id, status from BATCH_JOB_EXECUTION;
```

Inspect the metadata of step executions
```sql
select step_execution_id, job_execution_id, step_name, status, read_count, write_count, commit_count, rollback_count  from BATCH_STEP_EXECUTION;
```
