create table SubmittedRecipients (
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