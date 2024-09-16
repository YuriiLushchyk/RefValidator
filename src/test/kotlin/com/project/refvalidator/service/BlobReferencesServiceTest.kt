package com.project.refvalidator.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.rowset.SqlRowSet

@ExtendWith(MockKExtension::class)
class BlobReferencesServiceTest {

    @MockK
    lateinit var globalTemplate: JdbcTemplate

    @MockK
    lateinit var shardTemplate: JdbcTemplate

    @InjectMockKs
    lateinit var service: BlobReferencesService

    @Test
    fun `Giving ids in tables getBlobIdRanges`() {
        mockRangeQuery(globalTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM SentMessage", 13, 300)
        mockRangeQuery(globalTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM SentAttachment", 40, 50)
        mockRangeQuery(shardTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM Attachment", 4, 30)
        mockRangeQuery(shardTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM OutsideAttachment", 300, 1001)
        mockRangeQuery(shardTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM ContactData", null, null)
        mockRangeQuery(shardTemplate, "SELECT MIN(Body) AS minId, MAX(Body) AS maxId \nFROM MessageData", null, null)
        mockRangeQuery(shardTemplate, "SELECT MIN(Header) AS minId, MAX(Header) AS maxId \nFROM MessageData", 40, 700)
        mockRangeQuery(globalTemplate, "SELECT MIN(BlobStorageID) AS minId, MAX(BlobStorageID) AS maxId \nFROM BlobStorage", 700, 750)
        val result = service.getBlobIdRanges()
        assertEquals(4,result.first)
        assertEquals(1001,result.second)
    }


    private fun mockRangeQuery(jdbcTemplate: JdbcTemplate, query: String, minId: Long?, maxId: Long?) {
        val sqlRowSet = mockk<SqlRowSet>()
        every { sqlRowSet.next() } returns true
        every {
            jdbcTemplate.queryForRowSet(query)
        } returns sqlRowSet
        mockSqlRowSetGet(sqlRowSet, "minId", minId)
        mockSqlRowSetGet(sqlRowSet, "maxId", maxId)
    }

    private fun mockSqlRowSetGet(sqlRowSet: SqlRowSet, s: String, expectedResult: Long?) {
        every { sqlRowSet.getObject(s) } returns expectedResult
        if (expectedResult != null) {
            every { sqlRowSet.getLong(s) } returns expectedResult
        }
    }
}