package com.github.mkay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

@SpringBootApplication
class KafkaHttpSink {

    private val log = logger()

    private var countDown: CountDownLatch? = null

    val messages = mutableListOf<String>()

    fun expectedNumberOfMessages(numberOfMessages: Int) {
        countDown = CountDownLatch(numberOfMessages)
    }

    fun await(timeout: Long = 5, unit: TimeUnit = TimeUnit.SECONDS) {
        countDown?.await(timeout, unit) ?: throw IllegalStateException("No countdown set")
    }

    /* Does not work */
    @Bean
    fun sink(): Consumer<Message<List<String>>> {
        return Consumer {
            log.debug("Received ${it.payload.size} records(s)")
            val keys = it.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY] as List<String>
            it.payload.forEachIndexed { i, payload ->
                log.info("processing record key: ${keys[i]} value: $payload")
                messages.add(payload)
                countDown?.countDown()
            }
        }
    }

//    /* Works */
//    @Bean
//    fun sink(): Consumer<List<String>> {
//        return Consumer {
//            log.debug("Received ${it.size} records(s)")
//            it.forEach { message ->
//                log.info("processing record $message")
//                messages.add(message)
//                countDown?.countDown()
//            }
//        }
//    }
//
//    /* Works with `spring.cloud.stream.bindings.sink-in-0.consumer.batchMode=false`*/
//    @Bean
//    fun sink(): Consumer<Message<String>> {
//        return Consumer { message ->
//            log.info("processing single record key: ${message.headers[KafkaHeaders.RECEIVED_MESSAGE_KEY]} value: ${message.payload}")
//            countDown?.countDown()
//        }
//    }
}

fun main(args: Array<String>) {
    runApplication<KafkaHttpSink>(*args)
}
