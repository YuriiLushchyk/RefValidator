package com.project.refvalidator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
@EnableAsync
class RefValidatorApplication

fun main(args: Array<String>) {
    runApplication<RefValidatorApplication>(*args)
}


