select qrtoken from [dbo].[Recipients] where cid = 'YGN60000101'

select centerid, count(r.cid), sum(dose) from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
on r.pisyskey = pi.syskey where r.partnersyskey = 6389850392744707047 group by pi.centerid

select pi.centerid, c.centername, count(r.cid), sum(dose), max(cid), r.firstdosedate from [dbo].[Recipients] as r left join [dbo].[ProformaInvoice] as pi 
on r.pisyskey = pi.syskey left join [dbo].[Centers] as c on c.centerid = pi.centerid group by pi.centerid, c.centername, r.firstdosedate