use weibofollowersincreasor;

create table category1_type1_user_collected (
	id int not null auto_increment,
	user_id varchar(20) not null,
	user_name varchar(40) not null,
	primary key (id)
);

insert into category1_type1_user_collected (user_id, user_name) values 
('1618051664', '头条新闻'),
('2803301701', '人民日报'),
('2656274875', '央视新闻'),
('2286908003', '人民网'),
('1893801487', '微天下'),
('1314608344', '新闻晨报'),
('1742566624', '思想聚焦'),
('1699540307', '中国之声'),
('1644114654', '新京报'),
('1656831930', '环球资讯广播');

create table category1_type1_user_applying (
	id int not null auto_increment,
  	cookies text not null,
  	created_timestamp timestamp not null,
  	primary key (id)
);

create table category1_type1_follower_collected (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);

create table category1_type1_follower_filtered (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);

create table category1_type1_follower_followed (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);

create table category1_type1_follower_unfollowed (
	id int not null auto_increment,
	user_id varchar(20) not null,
	created_timestamp timestamp not null default current_timestamp,
	primary key (id)
);
