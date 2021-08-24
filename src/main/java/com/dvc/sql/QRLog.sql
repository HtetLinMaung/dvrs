create table QRLog (
    syskey bigint not null,
    cid nvarchar(255) not null,
    userid nvarchar(50) not null,
    verifyat datetime,
)