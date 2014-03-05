use weibofollowersincreasor;

create table category1_type1_user_collected (
	id int not null auto_increment,
	user_id varchar(20) not null,
	user_name varchar(40) not null,
	primary key (id)
);

insert into category1_type1_user_collected (user_id, user_name) values 
('2803301701', '人民日报'),
('2656274875', '央视新闻'),
('1893801487', '微天下'),
('1191965271', '三联生活周刊'),
('1653689003', '新周刊'),
('1231759973', '头条博客'),
('1742566624', '思想聚焦'),
('1699540307', '中国之声'),
('1644489953', '南方都市报'),
('1709157165', '联合国'),
('1635764393', '袁腾飞'),
('2277448183', '历史袁老师'),
('1644114654', '新京报'),
('1513934187', '新浪读书'),
('1708643783', '钱文忠'),
('1323527941', 'Vista看天下'),
('1653460650', '南方人物周刊'),
('1653603955', '扬子晚报'),
('1651428902', '21世纪经济报道'),
('1497087080', '羊城晚报'),
('1267454277', '凤凰周刊'),
('2034347300', '郑州晚报'),
('1641561812', '经济观察报'),
('1650111241', '中国经营报'),
('2307318984', '前史之鉴'),
('1702883112', '河南商报'),
('1149029297', '赫连勃勃大王'),
('2516798477', '新浪文化'),
('1645578093', '南风窗'),
('1703371307', '北京晚报'),
('2686904145', '我爱围观'),
('1700087532', '三湘都市报'),
('1749990115', '北京青年报'),
('2324974633', '微博读书'),
('1678302013', '新浪阅读'),
('2155226773', '杂谈微吧'),
('2053975695', '全球精华搜罗'),
('1728885872', '媒体小喇叭'),
('3114175427', '解放日报'),
('3297162173', '新浪专栏'),
('3225231435', '新浪历史'),
('1280846847', '沈阳晚报'),
('1801817195', '辽沈晚报'),
('1406387602', '时代迷思'),
('2155035021', '读书微吧'),
('1256970057', '时评聚焦'),
('3160227432', '文史经理'),
('1718493627', '瞭望'),
('1195889003', '葛剑雄'),
('2431328567', '微博新鲜事');

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
