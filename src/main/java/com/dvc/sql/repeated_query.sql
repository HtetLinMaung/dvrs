select qrtoken from [dbo].[Recipients] where cid = 'YGN60000101'

-- select centerid, count(r.cid), sum(dose) from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
-- on r.pisyskey = pi.syskey where r.partnersyskey = 6389850392744707047 group by pi.centerid

select pi.centerid, c.centername, count(r.cid), sum(dose), max(cid), r.firstdosedate from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
on r.pisyskey = pi.syskey left join [dbo].[Centers] as c on c.centerid = pi.centerid group by pi.centerid, c.centername, r.firstdosedate

-- scan not update
select r.cid, q.userid, q.verifyat, dose, recipientsname, fathername, nric, passport, dob, 
township, address1, mobilephone from [dbo].[Recipients] as r left join [dbo].[QRLog] as q on 
q.cid = r.cid where dose = 0 and DATEDIFF(day, verifyat, '2021/08/25') = 0 and (r.cid like 'YGN1%' or r.cid like 'YGN0%')

update [dbo].[ProformaInvoice] set qty = 15000, balance = 14969 where pirefnumber = 'PI000096'
select qty, balance, voidcount from [dbo].[ProformaInvoice] where pirefnumber = 'PI000096'





select distinct q.cid from [dbo].[QRLog] as q left join [dbo].[Recipients] as r on r.cid = q.cid where q.cid not in (select cid from [dbo].[DoseRecords] where DATEDIFF(day, doseupdatetime, '2021/08/25') = 0)