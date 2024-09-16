package com.project.refvalidator.model

import org.springframework.jdbc.core.JdbcTemplate

class ReferencesSource(val template: JdbcTemplate, val references: ReferenceLocation)

data class ReferenceLocation(val dataSourceName: String, val table: String, val column: String)