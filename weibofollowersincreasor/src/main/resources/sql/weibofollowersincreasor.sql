drop database weibofollowersincreasor;
create database if not exists weibofollowersincreasor default charset utf8 collate utf8_general_ci;

use weibofollowersincreasor;

create table user_collecting (
	id int not null auto_increment,
  	cookies text not null,
  	created_timestamp timestamp not null,
  	primary key (id)
);