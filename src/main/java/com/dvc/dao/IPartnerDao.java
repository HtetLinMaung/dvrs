package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.PartnerDto;

public interface IPartnerDao {

    PaginationResponse<Map<String, Object>> getPartners(FilterDto dto) throws SQLException;

    Map<String, Object> getPartner(long syskey) throws SQLException;

    int updatePartner(PartnerDto dto) throws SQLException, IOException;

    int deletePartner(PartnerDto dto) throws SQLException;

    long addPartner(PartnerDto dto) throws SQLException, IOException;
}
