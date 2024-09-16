package com.project.refvalidator.util


fun splitRange(min: Long, max: Long): List<Pair<Long, Long>> {
    val ranges = mutableListOf<Pair<Long, Long>>()
    var start = min
    while (start <= max) {
        val end = (start + BATCH_SIZE - 1).coerceAtMost(max)
        ranges.add(Pair(start, end))
        start = end + 1
    }
    return ranges
}

private const val BATCH_SIZE = 1000