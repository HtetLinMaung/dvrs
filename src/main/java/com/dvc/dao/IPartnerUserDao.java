package com.dvc.dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import com.dvc.models.FilterDto;
import com.dvc.models.PaginationResponse;
import com.dvc.models.PartnerUserDto;

public interface IPartnerUserDao {
    PaginationResponse<Map<String, Object>> getPartnerUsers(FilterDto dto) throws SQLException;

    Map<String, Object> getPartnerUser(long syskey) throws SQLException;

    int updatePartnerUser(PartnerUserDto dto) throws SQLException, IOException;

    int deletePartnerUser(PartnerUserDto dto) throws SQLException;

    long addPartnerUser(PartnerUserDto dto) throws SQLException, IOException;
}
