package com.moonactive.assignment.converter.mapper;

import com.moonactive.assignment.dao.LicensePlate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LicensePlateRowMapper implements RowMapper<LicensePlate> {
    @Override
    public LicensePlate mapRow(ResultSet rs, int rowNum) throws SQLException {
        LicensePlate licensePlate = new LicensePlate();
        licensePlate.setId(rs.getString("PLATE_ID"));
        licensePlate.setType(rs.getString("LICENSE_TYPE"));
        licensePlate.setTimeStamp(rs.getTimestamp("TS"));
        return licensePlate;
    }
}
