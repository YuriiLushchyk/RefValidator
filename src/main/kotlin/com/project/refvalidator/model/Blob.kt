package com.project.refvalidator.model

import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

data class Blob(val id: Long, val numReferences: Int) {

    class Mapper : RowMapper<Blob> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Blob {
            return Blob(rs.getLong("BlobStorageID"), rs.getInt("NumReferences"))
        }
    }
}
