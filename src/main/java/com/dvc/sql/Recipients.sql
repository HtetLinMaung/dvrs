create table Recipients (
    syskey bigint not null primary key,
    createddate datetime,
    modifieddate datetime,
    userid nvarchar(50),
    username nvarchar(50),
    recordstatus SMALLINT default 1,
    Remark nvarchar(255),
    rid bigint,
    cid nvarchar(50),
    RecipientsName nvarchar(255),
    FatherName nvarchar(255),
    Gender nvarchar(10),
    DoB nvarchar(255),
    Age int,
    NRIC nvarchar(50),
    Passport nvarchar(50),
    Nationality nvarchar(50),
    Organization nvarchar(255),
    Occupation nvarchar(50),
    Address1 nvarchar(255),
    Address2 nvarchar(255),
    Township nvarchar(255),
    Division nvarchar(255),
    MobilePhone nvarchar(50),
    RegisterationStatus tinyint,
    VaccinationStatus tinyint,
    PIRef nvarchar(50),
    BatchUploadSysKey bigint,
    PartnerSysKey bigint,
    PISysKey bigint,
    PaymentStatus SMALLINT default 0,
    VoidStatus SMALLINT default 0,
    QRToken TEXT default '',
    BatchRefCode nvarchar(50),
    dose int default 0, 
    firstdosedate nvarchar(255),
    firstdosetime nvarchar(255),
    seconddosetime nvarchar(255),
    centerid nvarchar(50) default '',
    prefixnrc nvarchar(255),
    nrccode nvarchar(255),
    nrctype nvarchar(255),
    nrcno nvarchar(255),
    ward nvarchar(255),
    street nvarchar(255),
    vaccinationcenter nvarchar(255),
    groupid nvarchar(255),
    isexported int default 0,
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
create index recipients_index on [dbo].[Recipients](cid, recordstatus)


update Recipients set centerid = 'YGN1' where cid like 'YGN1%' and len(cid) = 11; update Recipients set centerid = 'YGN2' where cid like 'YGN2%' and len(cid) = 11; update Recipients set centerid = 'YGN3' where cid like 'YGN3%' and len(cid) = 11; update Recipients set centerid = 'YGN4' where cid like 'YGN4%' and len(cid) = 11; update Recipients set centerid = 'YGN5' where cid like 'YGN5%' and len(cid) = 11; update Recipients set centerid = 'YGN6' where cid like 'YGN6%' and len(cid) = 11; update Recipients set centerid = 'YGN7' where cid like 'YGN7%' and len(cid) = 11; update Recipients set centerid = 'MDY1' where cid like 'MDY1%' and len(cid) = 11; update Recipients set centerid = 'MDY2' where cid like 'MDY2%' and len(cid) = 11; update Recipients set centerid = 'MDY3' where cid like 'MDY3%' and len(cid) = 11; update Recipients set centerid = 'NPW1' where cid like 'NPW1%' and len(cid) = 11; update Recipients set centerid = 'TGI1' where cid like 'TGI1%' and len(cid) = 11; update Recipients set centerid = 'TGI2' where cid like 'TGI2%' and len(cid) = 11; update Recipients set centerid = 'LSO1' where cid like 'LSO1%' and len(cid) = 11; update Recipients set centerid = 'LSO2' where cid like 'LSO2%' and len(cid) = 11; update Recipients set centerid = 'LSO3' where cid like 'LSO3%' and len(cid) = 11; update Recipients set centerid = 'LSO4' where cid like 'LSO4%' and len(cid) = 11; update Recipients set centerid = 'LSO5' where cid like 'LSO5%' and len(cid) = 11; update Recipients set centerid = 'MKN1' where cid like 'MKN1%' and len(cid) = 11; update Recipients set centerid = 'MKN2' where cid like 'MKN2%' and len(cid) = 11; update Recipients set centerid = 'MKN3' where cid like 'MKN3%' and len(cid) = 11; update Recipients set centerid = 'MKN4' where cid like 'MKN4%' and len(cid) = 11; update Recipients set centerid = 'MKN5' where cid like 'MKN5%' and len(cid) = 11; update Recipients set centerid = 'KOK1' where cid like 'KOK1%' and len(cid) = 11; update Recipients set centerid = 'KOK2' where cid like 'KOK2%' and len(cid) = 11; update Recipients set centerid = 'KOK3' where cid like 'KOK3%' and len(cid) = 11; update Recipients set centerid = 'KOK4' where cid like 'KOK4%' and len(cid) = 11; update Recipients set centerid = 'KOK5' where cid like 'KOK5%' and len(cid) = 11; update Recipients set centerid = 'KOK6' where cid like 'KOK6%' and len(cid) = 11; update Recipients set centerid = 'KOK7' where cid like 'KOK7%' and len(cid) = 11; update Recipients set centerid = 'TMN1' where cid like 'TMN1%' and len(cid) = 11; update Recipients set centerid = 'TYN1' where cid like 'TYN1%' and len(cid) = 11; update Recipients set centerid = 'TCK1' where cid like 'TCK1%' and len(cid) = 11; update Recipients set centerid = 'PSN1' where cid like 'PSN1%' and len(cid) = 11; update Recipients set centerid = 'PSN2' where cid like 'PSN2%' and len(cid) = 11; update Recipients set centerid = 'MSE1' where cid like 'MSE1%' and len(cid) = 11; update Recipients set centerid = 'PTN1' where cid like 'PTN1%' and len(cid) = 11; update Recipients set centerid = 'MLM1' where cid like 'MLM1%' and len(cid) = 11; update Recipients set centerid = 'MYK1' where cid like 'MYK1%' and len(cid) = 11; update Recipients set centerid = 'SGN1' where cid like 'SGN1%' and len(cid) = 11; update Recipients set centerid = 'DWI1' where cid like 'DWI1%' and len(cid) = 11; update Recipients set centerid = 'YGN' where cid like 'YGN%' and len(cid) = 10; update Recipients set centerid = 'YGN10' where cid like 'YGN10%' and len(cid) = 12; update Recipients set centerid = 'YGN11' where cid like 'YGN11%' and len(cid) = 12; update Recipients set centerid = 'YGN8' where cid like 'YGN8%' and len(cid) = 11; update Recipients set centerid = 'YGN9' where cid like 'YGN9%' and len(cid) = 11