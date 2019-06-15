-- Creates all required tables for application

create schema wallettest;

use wallettest;

create table logs (
    id int NOT NULL AUTO_INCREMENT,
    date DATETIME ,
    ip varchar(50),
    request varchar(50),
    status varchar(50),
    user_agent varchar(200),
    PRIMARY KEY (id)
);

create index logs_date_idx on logs (date);

create table blocks (
    id int NOT NULL AUTO_INCREMENT,
    ip varchar(50),
    reason varchar(100),
    PRIMARY KEY (id)
);