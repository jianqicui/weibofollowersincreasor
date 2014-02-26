use weibofollowersincreasor;

create table category1_type (
	id int not null auto_increment,
	type_id int not null,
	type_name varchar(10) not null,
	primary key (id)
);

insert into category1_type (type_id, type_name) values 
(1, '历史');