create table UserHistory (
    syskey bigint not null primary key,
    createddate datetime,
    modifieddate datetime,

    t1 nvarchar(50),
    t2 nvarchar(50),

    t3 nvarchar(255),
    t4 nvarchar(50),
    t5 nvarchar(50),
    t6 nvarchar(50),
    t7  nvarchar(255),
    t8 nvarchar(255),
    t9 nvarchar(255),

    t10 nvarchar(255),
    t11 nvarchar(50),
    t12 nvarchar(255),

    n1 tinyint,
    n2 SMALLINT,
    n3 bigint,
    n4 bigint
)