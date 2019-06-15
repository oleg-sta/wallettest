# Wallet test task

## What is it?

This is an implementation of Wallet test task.

# Table of Contests

- [What is it?](#what-is-it)
- [Assumptions](#assumptions)
- [Database scripts](#database_script)
- [Compiling](#compiling)
- [Starting](#starting)

## Assumptions
For working application it is reuired MySQL 5.7 or higher.
In example:
* java -cp "parser.jar" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
 we assume that at first we load data to DB, then make analytics from DB. So there is no analytics only on file.
Bockking by IP created with doubles IP.
There is no checking on invalid data in file.

## Database scripts

Scripts are situated in sql dir. Thre are 3 files:
 * init_db.sql - init tables
 * find_requests_by_ip.sql - example of select to find all requests by ip
 * find_ip_more_than.sql - example of select to find ip with more request in period of time

## Settings

For compiling you need to have maven 3.3.9 or above.
Start compiling by:
* mvn clean install
result file will be target/parser.jar

## Starting

In working directory you should have file config.properties with database connection properties. You could get it from root of source files.
Make required modofication in file. Start application as written in Test Instruction.


