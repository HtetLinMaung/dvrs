package com.dvc.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BankDao extends BaseDao implements IBankDao {

    @Override
    public List<Map<String, Object>> getBanks() throws SQLException {
        return getDBClient().getMany(Arrays.asList("bankname", "branchname", "bankrefnumber", "accountnumber"),
                "Banks");
    }

}
