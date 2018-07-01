package com.samples.consumers;

import akka.actor.AbstractActor;
import akka.stream.ActorMaterializer;
import akka.util.Timeout;
import lombok.extern.java.Log;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Log
public class KafkaConsumerActor extends AbstractActor {

    private final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    private final ActorMaterializer materializer = ActorMaterializer.create(getContext().getSystem());

    public KafkaConsumerActor() {
    }

    @Override
    public void preStart() {

    }

    @Override
    public void postStop() {
        materializer.shutdown();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, str -> {

                    log.info("starting consumer stream...");
                    startStream();

                })
                .build();
    }

    private void startStream() {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");


        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", "http://localhost:8081");

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        String topic = "topic1";
        final org.apache.kafka.clients.consumer.Consumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(props);
        consumer.subscribe(Arrays.asList(topic));

        try {
            while (true) {
                ConsumerRecords<String, GenericRecord> records = consumer.poll(100);
                for (ConsumerRecord<String, GenericRecord> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s \n", record.offset(), record.key(), record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
