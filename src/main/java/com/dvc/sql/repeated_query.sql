select qrtoken from [dbo].[Recipients] where cid = 'YGN60000101'

-- select centerid, count(r.cid), sum(dose) from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
-- on r.pisyskey = pi.syskey where r.partnersyskey = 6389850392744707047 group by pi.centerid

select pi.centerid, c.centername, count(r.cid), sum(dose), max(cid) from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
on r.pisyskey = pi.syskey left join [dbo].[Centers] as c on c.centerid = pi.centerid group by pi.centerid, c.centername

select centerid,count(r.cid) as voidcount from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi on pi.syskey = r.pisyskey where voidstatus = 0 group by centerid

select s1.centerid from (select pi.centerid, c.centername, count(r.cid) as cards, sum(dose) as doses, max(cid) as cid from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
on r.pisyskey = pi.syskey left join [dbo].[Centers] as c on c.centerid = pi.centerid group by pi.centerid, c.centername) as s1 left join (select centerid,count(r.cid) as voidcount from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi on pi.syskey = r.pisyskey where voidstatus = 0 group by centerid) as s2 on s1.centerid = s2.centerid

-- scan not update
select r.cid, q.userid, q.verifyat, dose, recipientsname, fathername, nric, passport, nationality, dob, 
township, address1, mobilephone from [dbo].[Recipients] as r left join [dbo].[QRLog] as q on 
q.cid = r.cid where dose = 0 and DATEDIFF(day, verifyat, '2021/09/04') = 0 and (r.cid like 'YGN10%' or r.cid like 'YGN0%')

update [dbo].[ProformaInvoice] set qty = 15000, balance = 14969 where pirefnumber = 'PI000096'
select qty, balance, voidcount from [dbo].[ProformaInvoice] where pirefnumber = 'PI000096'


-- nodosecount report
select partnerid, partnername, firstdosedate, count(cid) as nodosecount from [dbo].[Recipients] as r 
left join [dbo].[Partners] as p on r.partnersyskey = p.syskey 
where dose = 0 and firstdosedate in ('23/08/2021', '24/08/2021', '25/08/2021', '26/08/2021', '27/08/2021', '28/08/2021', '29/08/2021', '30/08/2021', '31/08/2021', '01/09/2021', '02/09/2021', '03/09/2021') group by partnerid, partnername, firstdosedate order by firstdosedate



select distinct q.cid from [dbo].[QRLog] as q left join [dbo].[Recipients] as r on r.cid = q.cid where q.cid not in (select cid from [dbo].[DoseRecords] where DATEDIFF(day, doseupdatetime, '2021/08/25') = 0)










update [dbo].[Recipients] set t10 = '1) 26/08/2021, NA Khin Myat Thu, lot 202107B1936, 2, 1; ',
 dose = 1 where cid = 'YGN10007485'
delete from [dbo].[DoseRecords] where syskey = 5952001945114819371
select * from [dbo].[DoseRecords] where cid = 'YGN10007485'
select t10 from [dbo].[Recipients] where cid = 'YGN10007485'



-- select cid, firstdosedate, firstdosetime, recipientsname, fathername, nric, passport, nationality, mobilephone, division, address1, mobilephone, remark from [dbo].[Recipients] where cid like 'YGN1%' and dose = 1 and (cast(replace(cid, 'YGN1', '') as int) > 11400 and cast(replace(cid, 'YGN1', '') as int) <= 12250 or cast(replace(cid, 'YGN1', '') as int) > 13950 and cast(replace(cid, 'YGN1', '') as int) <= 14800)


select cid, centerid from [dbo].[Recipients] 
where centerid = 'YGN1' and (cid between 'YGN10000001' and 'YGN10000003' or cid between 'YGN10000006' and 'YGN10000009') order by cid



select count(r.cid), partnersyskey from Recipients group by partnersyskey

select p.partnername,sum(validcount), p.partnersyskey from [dbo].[BatchUpload] as b left join [dbo].[Partners] as p on p.syskey = b.partnersyskey group by b.partnersyskey, p.partnername

-- select s1.partnername, s1.totalvalid, s2.totalrecipients from (select p.partnername, sum(validcount) as totalvalid, b.partnersyskey 
-- from [dbo].[BatchUpload] as b left join [dbo].[Partners] as p on 
-- p.syskey = b.partnersyskey where b.recordstatus >= 30 group by b.partnersyskey, p.partnername) as s1 left join (select count(cid) as totalrecipients, partnersyskey from Recipients group by partnersyskey) as s2 on s1.partnersyskey = s2.partnersyskey where s1.totalvalid <> s2.totalrecipients



select r.centerid, c.centername, count(r.cid) as cards, max(cid) as cid, sum(dose) as doses from [dbo].[Recipients] as r left join [dbo].[Centers] as c 
on c.centerid = r.centerid group by r.centerid, c.centername



select centerid, count(cid) as voidcount from [dbo].[Recipients] where voidstatus = 0 group by centerid

select s1.centerid, s1.centername, s1.cards, s1.cid, s1.doses, s2.voidcount from (select r.centerid, c.centername, count(r.cid) as cards, max(cid) as cid, sum(dose) as doses from [dbo].[Recipients] as r left join [dbo].[Centers] as c 
on c.centerid = r.centerid group by r.centerid, c.centername) as s1 left join (select centerid, count(cid) as voidcount from [dbo].[Recipients] where voidstatus = 0 group by centerid) as s2 on s1.centerid = s2.centerid





-- testing first dose date and second dose date query
select d1.cid, d1.doctor, d1.lot, d1.doseupdatetime, d2.doctor, d2.lot, d2.doseupdatetime from [dbo].[DoseRecords] as d1 
left join [dbo].[DoseRecords] as d2 on d1.cid = d2.cid where d1.cid = 'YGN10042551' 
and d1.doseupdatetime < d2.doseupdatetime