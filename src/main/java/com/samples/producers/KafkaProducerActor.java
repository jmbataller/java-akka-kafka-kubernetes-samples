package com.samples.producers;

import akka.actor.AbstractActor;
import akka.kafka.ConsumerSettings;
import akka.stream.ActorMaterializer;
import akka.util.Timeout;
import lombok.extern.java.Log;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import scala.concurrent.duration.Duration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Log
public class KafkaProducerActor extends AbstractActor {

    private final Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));
    private final ActorMaterializer materializer = ActorMaterializer.create(getContext().getSystem());

    public KafkaProducerActor() {
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

                    log.info("start producer stream");
                    startStream(str);

                })
                .build();
    }

    private void startStream(String value) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://localhost:8081");
        KafkaProducer producer = new KafkaProducer(props);

        String key = "key1";
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("f1", value);

        ProducerRecord<Object, Object> record = new ProducerRecord<>("topic1", key, avroRecord);
        try {
            producer.send(record);
        } catch(SerializationException e) {
            // may need to do something with it
        }
        // When you're finished producing records, you can flush the producer to ensure it has all been written to Kafka and
        // then close the producer to free its resources.
        finally {
            producer.flush();
            producer.close();
        }
    }
}
