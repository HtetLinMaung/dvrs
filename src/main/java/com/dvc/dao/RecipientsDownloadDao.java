package com.dvc.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.dvc.factory.DbFactory;
import com.dvc.models.RecipentsData;
import com.dvc.models.VaccinationRecordData;
import com.dvc.utils.PDFUtil;
import com.dvc.utils.Sender;
import com.dvc.utils.ServerUtil;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.storage.StorageException;

public class RecipientsDownloadDao {
    private static final int BATCH_UPLOAD_FINISHED_STATUS = 35;
    private static final int BATCH_UPLOAD_PENDING_STATUS = 30;

    public ArrayList<RecipentsData> getRecipentsListByBatch(long batchNo, int topLimit, int startRow,
            long partnerSyskey, boolean isAdmin, boolean isFileDownload) {
        ArrayList<RecipentsData> rList = new ArrayList<RecipentsData>();
        try (Connection conn = DbFactory.getConnection()) {
            String adminQuery = "";
            if (isFileDownload && !isAdmin) {
                adminQuery = " AND Recipients.PartnerSyskey = ?  ";
            }
            StringBuilder builder = new StringBuilder();
            if (topLimit > 0) {
                builder.append("SELECT " + " TOP(" + topLimit + ")"
                        + " syskey,cid,recipientsname,gender,age,nric,organization,address1,passport"
                        + ",mobilephone,qrtoken,pisyskey,BatchUploadSysKey,BatchRefCode,FatherName,Nationality,Dob,VoidStatus,firstdosedate,firstdosetime,seconddosetime "
                        + " FROM Recipients WHERE Recipients.BatchUploadSysKey=? " + adminQuery
                        + " AND Recipients.recordstatus=1 ORDER BY cid ");
            } else {
                builder.append("SELECT " + " syskey,cid,recipientsname,gender,age,nric,organization,address1,passport"
                        + ",mobilephone,qrtoken,pisyskey,BatchUploadSysKey,BatchRefCode,FatherName,Nationality,Dob,VoidStatus,firstdosedate,firstdosetime,seconddosetime "
                        + " FROM Recipients WHERE Recipients.BatchUploadSysKey=? " + adminQuery
                        + " AND Recipients.recordstatus=1 ORDER BY cid OFFSET ? ROWS FETCH FIRST ? ROWS ONLY ");
            }

            PreparedStatement ps = conn.prepareStatement(builder.toString());
            ps.setLong(1, batchNo);
            int row = 2;
            if (isFileDownload && !isAdmin) {
                ps.setLong(row++, partnerSyskey);
            }
            if (topLimit == 0) {
                ps.setInt(row++, startRow);
                ps.setInt(row++, PDFUtil.MAX_RECORDS_PER_PDF);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RecipentsData r = new RecipentsData();
                r.setSyskey(rs.getLong("syskey"));
                r.setRecipientsName(rs.getString("RecipientsName"));
                r.setGender(rs.getString("Gender"));
                if (r.getGender() != null && !r.getGender().isEmpty() && !r.getGender().equals("")) {
                    if (r.getGender().equalsIgnoreCase("M")) {
                        r.setGender("Male");
                    } else if (r.getGender().equalsIgnoreCase("F")) {
                        r.setGender("Female");
                    }
                }
                r.setAge(rs.getInt("Age"));
                r.setNRIC(rs.getString("NRIC"));
                r.setOccupation(rs.getString("organization"));
                r.setMobilePhone(rs.getString("MobilePhone"));
                r.setAddress1(rs.getString("Address1"));
                r.setPI(rs.getString("pisyskey"));
                r.setBatch(rs.getString("BatchUploadSysKey"));
                r.setCertificateID(rs.getString("cid"));
                r.setQrToken(rs.getString("qrtoken"));
                r.setPassport(rs.getString("passport"));
                r.setBatchRefCode(rs.getString("BatchRefCode"));
                r.setFatherName(rs.getString("FatherName"));
                r.setNationality(rs.getString("Nationality"));
                r.setDob((rs.getString("Dob")));
                r.setVoidStatus(rs.getInt("VoidStatus"));
                r.setFirstdosedate(rs.getString("firstdosedate"));
                r.setFirstdosetime(rs.getString("firstdosetime"));
                r.setSeconddosetime(rs.getString("seconddosetime"));
                r.setvArrayList(getVaccincationDataByRecipientSk(conn, r.getSyskey()));

                rList.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rList;
    }

    private ArrayList<VaccinationRecordData> getVaccincationDataByRecipientSk(Connection conn, long syskey) {
        ArrayList<VaccinationRecordData> vaccinationRecordDatas = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select VaccineCompany,VaccineLotNo,VaccinationDate,NextVaccinationDate,t1 as DR,CenterName,VaccinationStatus"
                            + " from VaccinationRecord where recordstatus=1 and n1=? order by VaccinationStatus");

            ps.setLong(1, syskey);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                VaccinationRecordData v = new VaccinationRecordData();
                v.setVaccineCompany(rs.getString("VaccineCompany"));
                v.setVaccineLotNo(rs.getString("VaccineLotNo"));
                v.setVaccinationDate(ServerUtil.datetimeToString(rs.getString("VaccinationDate")));
                v.setNextVaccinationDate(ServerUtil.datetimeToString(rs.getString("NextVaccinationDate")));
                v.setT1(rs.getString("DR"));
                v.setCenterName(rs.getString("CenterName"));
                v.setVaccinationStatus(rs.getInt("VaccinationStatus"));
                vaccinationRecordDatas.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vaccinationRecordDatas;
    }

    public RecipentsData getRecipentBySyskeyOrCID(long syskey, boolean isSyskey, String cid, long partnerSyskey,
            boolean isAdmin) {
        RecipentsData recipentsData = new RecipentsData();
        try (Connection conn = DbFactory.getConnection()) {
            StringBuilder builder = new StringBuilder();
            builder.append("SELECT syskey,cid,recipientsname,gender,age,nric,organization,address1,passport"
                    + ",mobilephone,qrtoken,pisyskey,BatchUploadSysKey,BatchRefCode,FatherName,Nationality,Dob,VoidStatus,firstdosedate,firstdosetime,seconddosetime  "
                    + " FROM Recipients WHERE ");
            if (isSyskey && !isAdmin) {
                builder.append(" Recipients.syskey =? and Recipients.PartnerSyskey = ? ");
            } else if (isSyskey && isAdmin) {
                builder.append(" Recipients.syskey =? ");
            } else {
                builder.append(" Recipients.cid =? ");
            }
            builder.append(" AND Recipients.recordstatus= 1 ");

            PreparedStatement ps = conn.prepareStatement(builder.toString());
            if (isSyskey && !isAdmin) {
                ps.setLong(1, syskey);
                ps.setLong(2, partnerSyskey);
            } else if (isSyskey && isAdmin) {
                ps.setLong(1, syskey);
            } else {
                ps.setString(1, cid);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                recipentsData.setSyskey(rs.getLong("syskey"));
                recipentsData.setRecipientsName(rs.getString("RecipientsName"));
                recipentsData.setGender(rs.getString("Gender"));
                if (recipentsData.getGender() != null && !recipentsData.getGender().isEmpty()
                        && !recipentsData.getGender().equals("")) {
                    if (recipentsData.getGender().equalsIgnoreCase("M")) {
                        recipentsData.setGender("Male");
                    } else if (recipentsData.getGender().equalsIgnoreCase("F")) {
                        recipentsData.setGender("Female");
                    }
                }
                recipentsData.setAge(rs.getInt("Age"));
                recipentsData.setNRIC(rs.getString("NRIC"));
                recipentsData.setOccupation(rs.getString("organization"));
                recipentsData.setMobilePhone(rs.getString("MobilePhone"));
                recipentsData.setAddress1(rs.getString("Address1"));
                recipentsData.setPI(rs.getString("pisyskey"));
                recipentsData.setBatch(rs.getString("BatchUploadSysKey"));
                recipentsData.setCertificateID(rs.getString("cid"));
                recipentsData.setQrToken(rs.getString("qrtoken"));
                recipentsData.setPassport(rs.getString("passport"));
                recipentsData.setBatchRefCode(rs.getString("BatchRefCode"));
                recipentsData.setFatherName(rs.getString("FatherName"));
                recipentsData.setNationality(rs.getString("Nationality"));
                recipentsData.setDob((rs.getString("Dob")));
                recipentsData.setVoidStatus(rs.getInt("VoidStatus"));
                recipentsData.setFirstdosedate(rs.getString("firstdosedate"));
                recipentsData.setFirstdosetime(rs.getString("firstdosetime"));
                recipentsData.setSeconddosetime(rs.getString("seconddosetime"));

                // String.format("|%020d|", 93);
                // CID-Name-NRIC/Passport
                String name = recipentsData.getCertificateID();
                if (!(recipentsData.getRecipientsName() == null || recipentsData.getRecipientsName().isEmpty()
                        || recipentsData.getRecipientsName().trim().isEmpty())) {
                    name += "-" + recipentsData.getRecipientsName();
                }
                if (!(recipentsData.getNRIC() == null || recipentsData.getNRIC().isEmpty()
                        || recipentsData.getNRIC().trim().isEmpty())) {
                    String nrcNo = recipentsData.getNRIC();
                    if (nrcNo.contains(")")) {
                        String[] arr = nrcNo.split("\\)");
                        if (arr.length > 1)
                            recipentsData.setPdfName(name + "-" + arr[1]);
                        else
                            recipentsData.setPdfName(name);
                    } else {
                        recipentsData.setPdfName(name + "-" + nrcNo);
                    }
                } else {
                    if (!(recipentsData.getPassport() == null || recipentsData.getPassport().isEmpty()
                            || recipentsData.getPassport().trim().isEmpty())) {
                        recipentsData.setPdfName(name + "-" + recipentsData.getPassport());
                    } else {
                        recipentsData.setPdfName(name);
                    }
                }

            }

            String sql1 = "select VaccineCompany,VaccineLotNo,VaccinationDate,NextVaccinationDate,t1 as DR,CenterName,VaccinationStatus"
                    + " from VaccinationRecord where recordstatus=1 and n1=? order by VaccinationStatus";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setLong(1, recipentsData.getSyskey());

            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                VaccinationRecordData v = new VaccinationRecordData();
                v.setVaccineCompany(rs1.getString("VaccineCompany"));
                v.setVaccineLotNo(rs1.getString("VaccineLotNo"));
                v.setVaccinationDate(ServerUtil.datetimeToString(rs1.getString("VaccinationDate")));
                v.setNextVaccinationDate(ServerUtil.datetimeToString(rs1.getString("NextVaccinationDate")));
                v.setT1(rs1.getString("DR"));
                v.setCenterName(rs1.getString("CenterName"));
                v.setVaccinationStatus(rs1.getInt("VaccinationStatus"));
                recipentsData.getvArrayList().add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipentsData;
    }

    public void updateBatchUploadStatus(long syskey) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BatchUpload SET recordstatus = ?, modifieddate= ? WHERE recordstatus = ? and syskey = ? ");
            ps.setInt(1, BATCH_UPLOAD_FINISHED_STATUS);
            ps.setObject(2, ldt.toString());
            ps.setInt(3, BATCH_UPLOAD_PENDING_STATUS);
            ps.setLong(4, syskey);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getBatchUploadStatusBySyskey(long syskey) {
        int status = 0;
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT recordstatus FROM BatchUpload WHERE syskey = ? ");
            ps.setLong(1, syskey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                status = rs.getInt("recordstatus");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    public int getTotalRecordsByBatchUploadSyskey(long syskey) {
        int totalCount = 0;
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "select count(*) as totalCount from Recipients where recordstatus = ? and BatchUploadSysKey = ?");
            ps.setInt(1, 1);
            ps.setLong(2, syskey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalCount = rs.getInt("totalCount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalCount;
    }

    public void updateRecordStatusBatchUpload(long syskey, double noOfTotalFiles, String folderName,
            ExecutionContext context) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            updateFinishedFileCount(syskey, noOfTotalFiles);
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BatchUpload SET recordstatus = ?, modifieddate= ? WHERE recordstatus = ? and syskey = ? and n1 = ? ");
            ps.setInt(1, BATCH_UPLOAD_FINISHED_STATUS);
            ps.setObject(2, ldt.toString());
            ps.setInt(3, BATCH_UPLOAD_PENDING_STATUS);
            ps.setLong(4, syskey);
            ps.setDouble(5, noOfTotalFiles);
            if (ps.executeUpdate() > 0) {
                new RecipientsDownloadDao().updateStartAndEndTime(syskey, false);
                try {
                    Map<String, Object> batch = new BatchUploadDao().getBatch(syskey);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date date = format.parse(Instant.now().toString());
                    Sender.sendEmail((String) batch.get("emailaddress"), String.format(
                            "<div>Dear User,</div>Your Record Card PDF is ready to download.</br></br>Partner ID: %s<br />Partner Name: %s<br />Batch Number: %s<br />Submission Date: %s<br />Thank you for using VRS 2021, RegistrationSystem.",
                            batch.get("partnerid"), batch.get("partnername"), batch.get("batchrefcode"),
                            new SimpleDateFormat("dd/MM/yyyy").format(date)), "VRS 2021, Registration System", "VRS");
                } catch (java.security.InvalidKeyException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (StorageException e) {
                    e.printStackTrace();
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTotalFilesBatchUpload(long syskey, double noOfTotalFiles, double totalRecords) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BatchUpload SET n1 = ?, n3 = ?, modifieddate= ? WHERE recordstatus = ? and syskey = ?");
            ps.setDouble(1, noOfTotalFiles);
            ps.setDouble(2, totalRecords);
            ps.setObject(3, ldt.toString());
            ps.setInt(4, BATCH_UPLOAD_PENDING_STATUS);
            ps.setLong(5, syskey);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFinishedFileCount(long syskey, double noOfAvailFiles) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE BatchUpload SET n2 = ?, modifieddate= ? WHERE recordstatus = ? and syskey = ? ");
            ps.setDouble(1, noOfAvailFiles);
            ps.setObject(2, ldt.toString());
            ps.setInt(3, BATCH_UPLOAD_PENDING_STATUS);
            ps.setLong(4, syskey);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStartAndEndTime(long syskey, boolean isStartTime) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            StringBuilder builder = new StringBuilder("UPDATE BatchUpload SET ");
            if (isStartTime) {
                builder.append(" t1 = ?, ");
            } else {
                builder.append(
                        " t2 = ?, t3 = CAST(((DATEDIFF(second, t1, ?))/60) as varchar(100)) + '.' + CAST(((DATEDIFF(second, t1, ?))%60) as nvarchar(100)) , ");
            }
            builder.append(" modifieddate= ? WHERE recordstatus = ? and syskey = ? ");
            PreparedStatement ps = conn.prepareStatement(builder.toString());
            ps.setObject(1, ldt.toString());
            ps.setObject(2, ldt.toString());
            if (isStartTime) {
                ps.setInt(3, BATCH_UPLOAD_PENDING_STATUS);
                ps.setLong(4, syskey);
            } else {
                ps.setObject(3, ldt.toString());
                ps.setObject(4, ldt.toString());
                ps.setInt(5, BATCH_UPLOAD_FINISHED_STATUS);
                ps.setLong(6, syskey);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(long syskey) {
        LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.of("+06:30"));
        try (Connection conn = DbFactory.getConnection()) {
            PreparedStatement ps = conn
                    .prepareStatement("UPDATE BatchUpload SET recordstatus = ?, modifieddate= ? WHERE syskey = ? ");
            ps.setInt(1, BATCH_UPLOAD_PENDING_STATUS);
            ps.setObject(2, ldt.toString());
            ps.setLong(3, syskey);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}