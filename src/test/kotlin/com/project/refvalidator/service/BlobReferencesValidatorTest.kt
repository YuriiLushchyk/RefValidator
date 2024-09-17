package com.project.refvalidator.service

import com.project.refvalidator.model.Blob
import com.project.refvalidator.model.ReferenceLocation
import com.project.refvalidator.model.ReferencesSource
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.jdbc.core.JdbcTemplate

@ExtendWith(MockKExtension::class)
class BlobReferencesValidatorTest {


    @MockK
    lateinit var blobReferencesService: BlobReferencesService

    @MockK
    lateinit var template: JdbcTemplate

    @InjectMockKs
    lateinit var validator: BlobReferencesValidator


    @Test
    fun `test asyncValidateReferencesForBlobsIn`() {
        val range = 11L to 50L
        val blobsInStorage = listOf(
                Blob(11L, 1),
                Blob(12L, 1),
                Blob(15L, 2),
                Blob(21L, 3),
                Blob(36L, 1),
                Blob(45L, 0)
        )

        val blobsReferred = listOf(
                Blob(11L, 1),
                Blob(12L, 2),
                Blob(15L, 1),
                Blob(21L, 3),
                Blob(33L, 1)
        )

        val singleSource = ReferencesSource(template, ReferenceLocation("database", "table", "column"))
        every { blobReferencesService.getBlobsFromStorageIn(range) } returns blobsInStorage
        every { blobReferencesService.getReferringDataSources() } returns listOf(singleSource)
        every { blobReferencesService.getBlobsReferredIn(range, singleSource) } returns blobsReferred

        val result = validator.asyncValidateReferencesForBlobsIn(range).join()
        assertEquals(4, result.size)
        assertEquals("Found record that refers to non existing blobId = 33. Datasource:database, table: table, column: column", result[0])
        assertEquals("Found blob with invalid NumReferences, BlobStorageID = 12, expected NumReferences = 1, but was actually referred 2 times.", result[1])
        assertEquals("Found blob with invalid NumReferences, BlobStorageID = 15, expected NumReferences = 2, but was actually referred 1 times.", result[2])
        assertEquals("Found blob with invalid NumReferences, BlobStorageID = 36, expected NumReferences = 1, but was actually referred 0 times.", result[3])
    }

}