TRUNCATE TABLE BatchUpload;
TRUNCATE TABLE BatchDetails;
TRUNCATE TABLE AttachmentFiles;
TRUNCATE TABLE CenterLastSerials;
TRUNCATE TABLE Partners;
TRUNCATE TABLE PartnerUser;
TRUNCATE TABLE ProformaInvoice;
TRUNCATE TABLE Recipients;
TRUNCATE TABLE UserHistory;
TRUNCATE TABLE VaccinationRecord;
TRUNCATE TABLE PostAttachmentFiles;
-- TRUNCATE TABLE Centers;

update Recipients set dose = 0, t10 = ''