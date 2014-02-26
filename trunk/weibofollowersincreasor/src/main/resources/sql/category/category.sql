use weibofollowersincreasor;

create table category (
	id int not null auto_increment,
	category_id int not null,
	category_name varchar(10) not null,
	primary key (id)
);

insert into category (category_id, category_name) values 
(1, '内部');
