package com.github.mkay

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.BinderFactory
import org.springframework.cloud.stream.binder.Binding
import org.springframework.cloud.stream.binder.kafka.KafkaBinderHealthIndicator
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder
import org.springframework.cloud.stream.binding.BindingBeanDefinitionRegistryUtils
import org.springframework.cloud.stream.binding.BindingService
import org.springframework.cloud.stream.binding.InputBindingLifecycle
import org.springframework.cloud.stream.config.BindingBeansRegistrar
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.messaging.MessageChannel
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092"])
class KafkaHttpSinkTests {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<Any, Any>

    @Autowired
    private lateinit var sink: KafkaHttpSink

    @Test
    fun `message consumed successfully`() {
        sink.expectedNumberOfMessages(2)

        kafkaTemplate.send(
            "input-topic",
            "1234",
            "some input message"
        )

        kafkaTemplate.send(
            "input-topic",
            "4321",
            "other input message"
        )

        sink.await(5, TimeUnit.SECONDS)

        assertThat(sink.messages).hasSize(2)
        assertThat(sink.messages).containsExactlyInAnyOrder("some input message", "other input message")
    }

}