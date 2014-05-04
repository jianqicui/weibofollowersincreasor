drop database weibofollowersincreasor;
create database if not exists weibofollowersincreasor default charset utf8 collate utf8_general_ci;

use weibofollowersincreasor;

create table user_querying (
	id int not null auto_increment,
  	cookies text not null,
  	created_timestamp timestamp not null,
  	primary key (id)
);

create table user_collecting (
	id int not null auto_increment,
	user_id varchar(20) not null,
	user_name varchar(40) not null,
	primary key (id)
);

insert into user_collecting (user_id, user_name) values 
('1618051664', '头条新闻'),
('1934183965', '微博管理员'),
('2656274875', '央视新闻'),
('2671109275', '新手指南'),
('2803301701', '人民日报');

create table user_collected (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);

create table user_filtered (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);