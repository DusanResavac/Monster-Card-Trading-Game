

drop table if exists trading_area;
drop table if exists stack_card;
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
    image varchar(255),
    coins integer,
    elo double precision,
    wins int,
    gamesPlayed int
);

create table session (
    id serial primary key,user_id integer,
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
    inDeck bool,
    foreign key (card_id) references card (id) on delete cascade,
    foreign key (user_id) references users (id) on delete cascade
);


create table trading_area (
    id varchar(255) primary key,
    card_id varchar(255),
    user_id integer,
    wantedType varchar(255),
    minimumDamage double precision,
    foreign key (card_id) references card (id) on delete cascade,
    foreign key (user_id) references users (id) on delete cascade
);
/*
create table deck_card (
    id serial primary key,
    card_id varchar(255),
    user_id integer,
    foreign key (card_id) references card (id) on delete cascade ,
    foreign key (user_id) references users (id) on delete cascade

);*/

delete from stack_card;
delete from package_card;
delete from trading_area;
delete from package;
delete from card;
delete from session;
delete from users;



insert into users (username, password, name, bio, image, coins, elo) values
('admin', '3dd487570bbf0bc9abcbb98d7a738afca320bd984156c090b3732be75dfee6246a9ad33d28b9f7bbdecc0a723770fab0561fdc623fb3bd920905342ccd746e82', null, null, 'https://www.memesmonkey.com/images/memesmonkey/8c/8c4fafb301810373c6e37285e9ec7b03.jpeg', 20, 100);
insert into session (user_id, token, createdAt) values
((select id from users where username = 'admin'), 'admin-mtcgToken', now());


insert into users (username, password, name, bio, image, coins, elo) values
('kienboec', '3dd487570bbf0bc9abcbb98d7a738afca320bd984156c090b3732be75dfee6246a9ad33d28b9f7bbdecc0a723770fab0561fdc623fb3bd920905342ccd746e82', null, null, null, 20, 100);
insert into session (user_id, token, createdAt) values
((select id from users where username = 'kienboec'), 'kienboec-mtcgToken', now());

--insert into package default values;

select username, coins, elo, wins, gamesPlayed from users;
select * from users;
select * from session;
select * from package;
select * from card;
select * from package_card;
select card_id, damage, element, type, user_id from stack_card join card c on stack_card.card_id = c.id;
select * from trading_area;


select * from card order by id;

select row_number() over (order by elo, wins, users.id desc), elo, gamesplayed, wins, username, token, users.id from users
    left join session s on users.id = s.user_id;

update users set elo = 100, wins = 0, gamesPlayed = 0;

select  row_number() over (order by elo desc, wins desc, users.id desc), elo, gamesplayed, wins, username from users
    order by elo desc, wins desc, users.id desc;

select minimumDamage, wantedType, damage, type, element, username from trading_area
    join stack_card sc on trading_area.card_id = sc.card_id
    join card c on trading_area.card_id = c.id and sc.card_id = c.id
    join users u on trading_area.user_id = u.id and sc.user_id = u.id;