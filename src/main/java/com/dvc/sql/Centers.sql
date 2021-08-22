create table Centers (
    syskey bigint not null primary key,
    createddate datetime,
    modifieddate datetime,
    userid nvarchar(50),
    username nvarchar(50),
    recordstatus SMALLINT default 1,
    remark nvarchar(255) default '',
    centerid nvarchar(50) not null,
    centername nvarchar(255) default '',
    centeraddress nvarchar(255) default '',
    allowblank int default 0,
    price decimal(18, 2) default 50000,
    t1 nvarchar(255),
    t2 nvarchar(255),
    t3 nvarchar(255),
    t4 nvarchar(255),
    t5 nvarchar(255),
    t6 nvarchar(255),
    t7 nvarchar(255),
    t8 nvarchar(255),
    t9 nvarchar(255),
    t10 nvarchar(255),
    n1 numeric(18, 2),
    n2 numeric(18, 2),
    n3 numeric(18, 2),
    n4 numeric(18, 2), 
    n5 numeric(18, 2),
    n6 numeric(18, 2),
    n7 numeric(18, 2),
    n8 numeric(18, 2),
    n9 numeric(18, 2), 
    n10 numeric(18, 2),
)























insert into Centers (syskey, centerid, centername, price, allowblank) values (1,'YGN1', 'Thuwunnabhumi Event Park', '50000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (2,'YGN2', 'Pun Hlaing Hospital', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (3,'YGN3', 'Pun Hlaing (Star City)', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (4,'YGN4', 'Grand Hanthar Hospital', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (5,'YGN5', 'Asia Royal Hospital', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (6,'YGN6', 'Victoria Hosptial', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (7,'YGN7', 'Pinlon Hospital', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (8,'MDY1', 'Yunnan Association 1', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (9,'MDY2', 'Yunnan Association 2', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (10,'MDY3', 'City Hospital', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (11,'NPW1', 'Naypyitaw', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (12,'TGI1', 'Fujian Association', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (13,'TGI2', 'Yunnan Association', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (14,'LSO1', 'Lashio 1', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (15,'LSO2', 'Lashio 2', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (16,'LSO3', 'Lashio 3', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (17,'LSO4', 'Lashio 4', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (18,'LSO5', 'Lashio 5', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (19,'MKN1', 'Myitkyina 1', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (20,'MKN2', 'Myitkyina 2', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (21,'MKN3', 'Myitkyina 3', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (22,'MKN4', 'Myitkyina 4', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (23,'MKN5', 'Myitkyina 5', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (24,'KOK1', 'Kokant 1', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (25,'KOK2', 'Kokant 2', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (26,'KOK3', 'Kokant 3', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (27,'KOK4', 'Kokant 4', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (28,'KOK5', 'Kokant 5', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (29,'KOK6', 'Kokant 6', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (30,'KOK7', 'Kokant 7', '40000', 1);
insert into Centers (syskey, centerid, centername, price, allowblank) values (31,'TMN1', 'Tamoenyin', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (32,'TYN1', 'Tangyan', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (33,'TCK1', 'Tachileik', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (34,'PSN1', 'Kyu Kote Pansai 1', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (35,'PSN2', 'Kyu Kote Pansai 2', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (36,'MSE1', 'Muse', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (37,'PTN1', 'Pathein', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (38,'MLM1', 'Mawlamyaing', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (39,'MYK1', 'Myeik', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (40,'SGN1', 'Sagaing', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank) values (41,'DWI1', 'Dawei 1', '40000', 0);
insert into Centers (syskey, centerid, centername, price, allowblank, recordstatus) values (42,'YGN', 'Reserved for adminstration', '50000', 0, 2);

