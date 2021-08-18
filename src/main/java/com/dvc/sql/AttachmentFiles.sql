create table AttachmentFiles (
    syskey bigint not null primary key,
    createddate datetime,
    modifieddate datetime,
    userid nvarchar(50),
    username nvarchar(50),
    recordstatus SMALLINT default 1,
    filename text default '',
    description text default '',
    pisyskey bigint not null,
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