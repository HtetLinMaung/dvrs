create table MohsExcelFiles (
    syskey bigint not null primary key,
    createddate datetime,
    modifieddate datetime,
    userid nvarchar(50),
    username nvarchar(50),
    recordstatus SMALLINT default 1,
    groupcode nvarchar(255) not null,
    subgroupcode nvarchar(255) not null,
    filename ntext not null,
)