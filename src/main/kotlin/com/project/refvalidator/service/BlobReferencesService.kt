package com.project.refvalidator.service

import com.project.refvalidator.model.Blob
import com.project.refvalidator.model.ReferenceLocation
import com.project.refvalidator.model.ReferencesSource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.rowset.SqlRowSet
import org.springframework.stereotype.Service

@Service
class BlobReferencesService(val globalTemplate: JdbcTemplate, val shardTemplate: JdbcTemplate) {

    fun getBlobIdRanges(): Pair<Long, Long> {
        val allSources = getReferringDataSources() + ReferencesSource(globalTemplate, globalBlobsSource)
        val rangesOfSource = allSources
                .map { source -> getBlobIdRanges(source) }
                .filter { range -> range.first != null && range.second != null }
                .map { range -> range.first!! to range.second!! }

        return rangesOfSource.minOf { it.first } to rangesOfSource.maxOf { it.second }
    }

    fun getBlobsFromStorageIn(range: Pair<Long, Long>): List<Blob> {
        return globalTemplate.query(GET_BLOBS_QUERY, Blob.Mapper(), range.first, range.second)
    }

    fun getReferringDataSources(): List<ReferencesSource> {
        return globalReferencesSources.map { ReferencesSource(globalTemplate, it) } +
                shardReferencesSources.map { ReferencesSource(shardTemplate, it) }
    }

    fun getBlobsReferredIn(idRange: Pair<Long, Long>, source: ReferencesSource): List<Blob> {
        return source.run {
            val query = """
                SELECT ${references.column} as BlobStorageID, count(*) as NumReferences
                FROM ${references.table}
                WHERE ${references.column} >= ${idRange.first} AND ${references.column} <= ${idRange.second}
                GROUP BY ${references.column}
            """.trimIndent()
            template.query(query, Blob.Mapper())
        }
    }

    private fun getBlobIdRanges(source: ReferencesSource): Pair<Long?, Long?> {
        val result = source.template.queryForRowSet("""
            SELECT MIN(${source.references.column}) AS minId, MAX(${source.references.column}) AS maxId 
            FROM ${source.references.table}
        """.trimIndent())
        result.next()
        return result.getNullableLong("minId") to result.getNullableLong("maxId")
    }

    fun SqlRowSet.getNullableLong(name: String): Long? {
        return if (this.getObject(name) == null) null else this.getLong(name)
    }

    companion object {
        val globalBlobsSource = ReferenceLocation("ProtonMailGlobal", "BlobStorage", "BlobStorageID")

        val globalReferencesSources = listOf(
                ReferenceLocation("ProtonMailGlobal", "SentMessage", "BlobStorageID"),
                ReferenceLocation("ProtonMailGlobal", "SentAttachment", "BlobStorageID")
        )

        val shardReferencesSources = listOf(
                ReferenceLocation("ProtonMailShard", "Attachment", "BlobStorageID"),
                ReferenceLocation("ProtonMailShard", "OutsideAttachment", "BlobStorageID"),
                ReferenceLocation("ProtonMailShard", "ContactData", "BlobStorageID"),
                ReferenceLocation("ProtonMailShard", "MessageData", "Body"),
                ReferenceLocation("ProtonMailShard", "MessageData", "Header")
        )

        const val GET_BLOBS_QUERY = "SELECT BlobStorageID, NumReferences FROM BlobStorage WHERE BlobStorageID >= ? AND BlobStorageID <= ?"
    }
}