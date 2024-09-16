package com.project.refvalidator.model

data class NumReferences(val expectedNumReferences: Int) {
    var actualNumReferences: Int = 0

    fun recordReferences(count: Int) {
        actualNumReferences += count
    }

    fun isNumReferencesInvalid(): Boolean {
        return actualNumReferences != expectedNumReferences
    }
}