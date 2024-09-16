package com.project.refvalidator.util


fun Pair<Long, Long>.splitRange(): List<Pair<Long, Long>> {
    val ranges = mutableListOf<Pair<Long, Long>>()
    var start = first
    while (start <= second) {
        val end = (start + BATCH_SIZE - 1).coerceAtMost(second)
        ranges.add(Pair(start, end))
        start = end + 1
    }
    return ranges
}

private const val BATCH_SIZE = 1000