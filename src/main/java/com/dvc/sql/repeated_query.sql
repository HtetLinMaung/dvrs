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
q.cid = r.cid where dose = 0 and DATEDIFF(day, verifyat, '2021/08/25') = 0 and (r.cid like 'YGN1%' or r.cid like 'YGN0%')

update [dbo].[ProformaInvoice] set qty = 15000, balance = 14969 where pirefnumber = 'PI000096'
select qty, balance, voidcount from [dbo].[ProformaInvoice] where pirefnumber = 'PI000096'





select distinct q.cid from [dbo].[QRLog] as q left join [dbo].[Recipients] as r on r.cid = q.cid where q.cid not in (select cid from [dbo].[DoseRecords] where DATEDIFF(day, doseupdatetime, '2021/08/25') = 0)










update [dbo].[Recipients] set t10 = '1) 26/08/2021, NA Khin Myat Thu, lot 202107B1936, 2, 1; ',
 dose = 1 where cid = 'YGN10007485'
delete from [dbo].[DoseRecords] where syskey = 5952001945114819371
select * from [dbo].[DoseRecords] where cid = 'YGN10007485'
select t10 from [dbo].[Recipients] where cid = 'YGN10007485'



