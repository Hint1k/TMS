/* PostgreSQL script */

\connect postgres

drop database if exists tms with (force);
create database tms;

\connect tms

drop schema if exists tms;
create schema tms;

set search_path = tms, public;

drop table if exists tms.users;
create table tms.users
(
    id       bigserial primary key,
    username varchar(45)  not null,
    email    varchar(100) not null unique,
    password varchar(68)  not null,
    enabled  boolean      not null
);

drop table if exists tms.authorities;
create table tms.authorities
(
    id        bigserial primary key,
    authority varchar(45) not null,
    user_id   bigserial   not null,
    foreign key (user_id) references tms.users (id)
);

drop table if exists tms.tasks;
create table tms.tasks
(
    id          bigserial primary key,
    name        varchar(255) not null,
    description text         not null,
    status      varchar(45)  not null,
    priority    varchar(45)  not null,
    author_id   bigserial    not null,
    assignee_id bigserial    not null,
    version     bigint       not null default 0,
    foreign key (author_id) references tms.users (id),
    foreign key (assignee_id) references tms.users (id)
);

drop table if exists tms.comments;
create table tms.comments
(
    id      bigserial primary key,
    text    text      not null,
    user_id bigserial not null,
    task_id bigserial not null,
    version bigint    not null default 0,
    foreign key (user_id) references tms.users (id),
    foreign key (task_id) references tms.tasks (id)
);

insert into tms.users (id, username, email, password, enabled)
values (1, 'admin', 'admin@example.com',
        '$2a$10$U.TJCuMA4c6lka5Xq7i43OK9iDoA1/niZU3Gi6Xez1JzB7wNwvQzu', true),
       (2, 'user1', 'user1@example.com',
        '$2a$10$U.TJCuMA4c6lka5Xq7i43OK9iDoA1/niZU3Gi6Xez1JzB7wNwvQzu', true),
       (3, 'user2', 'user2@example.com',
        '$2a$10$U.TJCuMA4c6lka5Xq7i43OK9iDoA1/niZU3Gi6Xez1JzB7wNwvQzu', true);

insert into tms.authorities (id, authority, user_id)
values (1, 'ROLE_ADMIN', 1),
       (2, 'ROLE_USER', 2),
       (3, 'ROLE_USER', 3);

insert into tms.tasks (id, name, description, status, priority, author_id, assignee_id, version)
values (1, 'Task1', 'Descr for Task1', 'PENDING', 'HIGH', 1, 2, 0),
       (2, 'Task2', 'Descr for Task2', 'PROCESSING', 'MEDIUM', 2, 3, 0),
       (3, 'Task3', 'Descr for Task3', 'COMPLETED', 'LOW', 3, 1, 0),
       (4, 'Task4', 'Descr for Task4', 'PENDING', 'LOW', 1, 2, 0),
       (5, 'Task5', 'Descr for Task5', 'PROCESSING', 'MEDIUM', 2, 3, 0),
       (6, 'Task6', 'Descr for Task6', 'COMPLETED', 'HIGH', 3, 1, 0),
       (7, 'Task7', 'Descr for Task7', 'PENDING', 'MEDIUM', 1, 2, 0),
       (8, 'Task8', 'Descr for Task8', 'PROCESSING', 'HIGH', 2, 3, 0),
       (9, 'Task9', 'Descr for Task9', 'COMPLETED', 'LOW', 3, 1, 0),
       (10, 'Task10', 'Descr for Task10', 'PENDING', 'HIGH', 1, 2, 0);

insert into tms.comments (id, text, user_id, task_id, version)
values (1, 'Comment for Task 1 by User 1', 2, 1, 0),
       (2, 'Comment for Task 2 by User 2', 3, 2, 0),
       (3, 'Comment for Task 3 by User 3', 1, 3, 0),
       (4, 'Comment for Task 4 by User 1', 2, 4, 0),
       (5, 'Comment for Task 5 by User 2', 3, 5, 0),
       (6, 'Comment for Task 6 by User 3', 1, 6, 0),
       (7, 'Comment for Task 7 by User 1', 2, 7, 0),
       (8, 'Comment for Task 8 by User 2', 3, 8, 0),
       (9, 'Comment for Task 9 by User 3', 1, 9, 0),
       (10, 'Comment for Task 10 by User 1', 2, 10, 0);

select setval('tms.tasks_id_seq', (select COALESCE(MAX(id), 0) from tasks) + 1, false);
select setval('tms.comments_id_seq', (select COALESCE(MAX(id), 0) from comments) + 1, false);
select setval('tms.users_id_seq', (select COALESCE(MAX(id), 0) from users) + 1, false);
select setval('tms.authorities_id_seq', (select COALESCE(MAX(id), 0) from authorities) + 1, false);