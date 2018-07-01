package com.samples;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.samples.consumers.KafkaConsumerActor;
import com.samples.producers.KafkaProducerActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.java.Log;

@Log
public class Main {

    public static void main(String[] args) {

        final Config config = ConfigFactory.load();

        ActorSystem system = ActorSystem.create("samples-service", config);


        ActorRef kafkaConsumer = system.actorOf(Props.create(KafkaConsumerActor.class), "kafkaConsumer");


        ActorRef kafkaProducer = system.actorOf(Props.create(KafkaProducerActor.class), "kafkaProducer");


        kafkaConsumer.tell("start", ActorRef.noSender());
        kafkaProducer.tell("hello world", ActorRef.noSender());


    }
}
