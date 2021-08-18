SELECT TOP (1000) * FROM [dbo].[Partners]
insert into [dbo].[Partners] (syskey, createddate, modifieddate, recordstatus, remark, partnertype, partnername,address,contactperson, emailaddress, contactnumber)
values (8415004913499000656, '20210725', '20210725', 1, 'mock', 20, 'ABC Association','Insein', 'Kaung Khant Kyaw', 'kaung.kk@mit.com.mm', '+959404888722')
insert into [dbo].[PartnerUser] (syskey, createddate, modifieddate, remark, role, partnersyskey, emailaddress) 
values (2317303862785687896, '20210725', '20210725', 'Add Partner', 'Partner', 8415004913499000656, 'kaung.kk@mit.com.mm')

insert into [dbo].[Partners] (syskey, createddate, modifieddate, recordstatus, remark, partnertype, partnername,address,contactperson, emailaddress, contactnumber)
values (8415004913499657656, '20210725', '20210725', 1, 'mock', 20, 'ABC Association','Insein', 'Kaung Khant Kyaw', 'kaung.kk@mit.com.mm', '+959404888722')