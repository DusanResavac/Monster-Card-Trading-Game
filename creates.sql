drop table if exists trading_area;
drop table if exists stack_card;
drop table if exists deck_card;
drop table if exists package_card;
drop table if exists package;
drop table if exists card;
drop table if exists session;
drop table if exists users;



create table users (
    id serial primary key,
    username varchar(255) unique not null,
    password varchar(255) not null,
    name varchar(255),
    bio varchar(500),
    image varchar(255)
);

create table session (
    id serial primary key,
    user_id integer,
    token varchar(255),
    createdAt timestamp,
    foreign key (user_id) references users (id) on delete cascade
);

create table package (
    id serial primary key
);

create table card (
    id varchar(255) primary key,
    type varchar(255) not null,
    damage double PRECISION,
    element varchar(255)
);

create table package_card (
    package_id integer,
    card_id varchar(255),
    primary key (package_id, card_id),
    foreign key (card_id) references card (id) on delete cascade,
    foreign key (package_id) references package (id) on delete cascade
);


create table stack_card (
    id serial primary key,
    card_id varchar(255),
    user_id integer,
    locked bool,
    foreign key (card_id) references card (id) on delete cascade,
    foreign key (user_id) references users (id) on delete cascade
);

create table deck_card (
    id serial primary key,
    card_id varchar(255),
    user_id integer,
    foreign key (card_id) references card (id) on delete cascade ,
    foreign key (user_id) references users (id) on delete cascade

);

create table trading_area (
    id varchar(255) primary key,
    card_id varchar(255),
    user_id integer,
    type varchar(255),
    element varchar(255),
    foreign key (card_id) references card (id) on delete cascade,
    foreign key (user_id) references users (id) on delete cascade
);


delete from trading_area;
delete from stack_card;
delete from deck_card;
delete from package_card;
delete from package;
delete from card;
delete from session;
delete from users;

select * from users;
select * from session;
insert into session(user_id, token) values (236, 'token-altenhof');
