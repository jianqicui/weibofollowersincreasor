use weibofollowersincreasor;

create table category1_type1_user_collected (
	id int not null auto_increment,
	user_id varchar(20) not null,
	user_name varchar(40) not null,
	primary key (id)
);

insert into category1_type1_user_collected (user_id, user_name) values 
('1635764393', '袁腾飞'),
('1708643783', '钱文忠'),
('2277448183', '历史袁老师'),
('1149029297', '赫连勃勃大王'),
('3225231435', '新浪历史'),
('1876807691', '历史一问一答'),
('1760369714', '历史震惊你'),
('2521277214', '这不是历史'),
('2161578435', '历史最最最震惊'),
('1299654943', '看历史'),
('1980929593', '历史上的今天'),
('1949520584', '历史大杂烩'),
('2193286573', '全球历史真相'),
('2348807884', '精彩历史'),
('2240572347', '李刚的私家历史'),
('1610362247', '国家人文历史'),
('2667738193', '历史的烟头'),
('2174462670', '历史都是重口味'),
('2040160344', '历史这玩意儿'),
('2662423037', '历史那点事儿'),
('2256841532', '历史断片'),
('2381967373', '历史尘封档案'),
('2250108614', '历史小百科');

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
