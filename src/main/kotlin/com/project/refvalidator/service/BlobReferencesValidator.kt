package com.project.refvalidator.service

import com.project.refvalidator.model.Blob
import com.project.refvalidator.model.NumReferences
import com.project.refvalidator.model.ReferencesSource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture


@Component
class BlobReferencesValidator(val blobReferencesService: BlobReferencesService) {

    @Async
    fun asyncValidateReferencesForBlobsIn(idRange: Pair<Long, Long>): CompletableFuture<List<String>> {
        val blobsFromStorage = blobReferencesService.getBlobsFromStorageIn(idRange)
        println("now = ${System.currentTimeMillis()} ,thread = ${Thread.currentThread().id}, idRange = $idRange, batch size ${blobsFromStorage.size}")

        val auditMap = transformToAuditMap(blobsFromStorage)
        val referringDataSources = blobReferencesService.getReferringDataSources()

        val results = mutableListOf<String>()

        referringDataSources.forEach {
            results.addAll(auditSource(idRange, it, auditMap))
        }

        results.addAll(finalCheck(auditMap))

        return CompletableFuture.completedFuture(results)
    }

    private fun finalCheck(auditMap: Map<Long, NumReferences>): List<String> {
        val result = mutableListOf<String>()
        auditMap.entries.forEach { blobRecord ->
            if (blobRecord.value.isNumReferencesInvalid()) {
                result.add("Found blob with invalid NumReferences, " +
                        "BlobStorageID = ${blobRecord.key}, expected NumReferences = ${blobRecord.value.expectedNumReferences}, but was actually referred ${blobRecord.value.actualNumReferences} times.")
            }
        }
        return result
    }

    private fun auditSource(idRange: Pair<Long, Long>,
                            source: ReferencesSource,
                            auditMap: Map<Long, NumReferences>): List<String> {
        val result = mutableListOf<String>()
        val blobsReferredInSource = blobReferencesService.getBlobsReferredIn(idRange, source)
        blobsReferredInSource.forEach { blobRef ->
            if (auditMap.containsKey(blobRef.id)) {
                auditMap[blobRef.id]?.recordReferences(blobRef.numReferences)
            } else {
                with(source.references) {
                    result.add("Found record that refers to non existing blobId = ${blobRef.id}. " +
                            "Datasource:$dataSourceName, table: $table, column: $column")
                }
            }
        }
        return result
    }

    private fun transformToAuditMap(blobs: List<Blob>): Map<Long, NumReferences> {
        return blobs.associate { blob -> blob.id to NumReferences(blob.numReferences) }
    }
}
