package com.project.refvalidator

import com.project.refvalidator.service.BlobReferencesService
import com.project.refvalidator.service.BlobReferencesValidator
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class StartupListener(val validator: BlobReferencesValidator,
                      val blobReferencesService: BlobReferencesService) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val blobIdRanges = blobReferencesService.getBlobIdRange().splitRange(MAX_BATCH_SIZE)
        val featureList = mutableListOf<CompletableFuture<List<String>>>() //features allowing us to collect errors from different threads

        blobIdRanges.forEach { featureList.add(validator.asyncValidateReferencesForBlobsIn(it)) } // run each batch in parallel

        CompletableFuture.allOf(*featureList.toTypedArray()).thenRun {
            val errors = mutableListOf<String>()
            featureList.forEach { errors.addAll(it.join()) }
            if (errors.isEmpty()) {
                println("All good!")
            } else {
                println("Not good, here are errors:")
                errors.forEach { println(it) }
            }
        }
    }


    companion object {
        private const val MAX_BATCH_SIZE = 1000

        fun Pair<Long, Long>.splitRange(maxBatchSize: Int): List<Pair<Long, Long>> {
            val ranges = mutableListOf<Pair<Long, Long>>()
            var start = first
            while (start <= second) {
                val end = (start + maxBatchSize - 1).coerceAtMost(second)
                ranges.add(Pair(start, end))
                start = end + 1
            }
            return ranges
        }
    }

}