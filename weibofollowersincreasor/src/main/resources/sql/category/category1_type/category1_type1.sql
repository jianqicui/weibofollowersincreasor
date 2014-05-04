use weibofollowersincreasor;

create table category1_type1_user_applying (
	id int not null auto_increment,
  	cookies text not null,
  	following_index int not null default 0,
  	created_timestamp timestamp not null,
  	primary key (id)
);

create table category1_type1_user1_followed (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);
