package com.project.refvalidator

import com.project.refvalidator.service.BlobReferencesService
import com.project.refvalidator.service.BlobReferencesValidator
import com.project.refvalidator.util.splitRange
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class StartupListener(val validator: BlobReferencesValidator,
                      val blobReferencesService: BlobReferencesService) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val blobIdRanges = blobReferencesService.getBlobIdRanges().splitRange()
        val featureList = mutableListOf<CompletableFuture<List<String>>>()

        blobIdRanges.forEach { featureList.add(validator.asyncValidateReferencesForBlobsIn(it)) }

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

}