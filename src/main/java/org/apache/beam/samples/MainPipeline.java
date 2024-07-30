/**
* Copyright 2024 Google LLC

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

* https://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**/
package org.apache.beam.samples;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.apache.beam.sdk.values.KV;
import java.nio.charset.StandardCharsets;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessageWithAttributesCoder;


public class MainPipeline {
  private static final Logger LOG = LoggerFactory.getLogger(MainPipeline.class);
  static void runKafka2Pubsub(StreamPipelineOptions options) {
    Pipeline p = Pipeline.create(options);
  
    Map<String, Object> consumerConfig = ImmutableMap.of(
      "security.protocol", "SSL",
      "ssl.truststore.location", options.getSslTruststoreLocation(),
      //"ssl.truststore.password", SecretManagerUtils.getSecret(options.getSecretManagerProjectId(), options.getSslTruststorePassSecretId(), "latest"),
      "ssl.truststore.password", options.getSslTruststorePassSecretId(),
      "ssl.keystore.location", options.getSslKeystoreLocation(),
      //"ssl.keystore.password", SecretManagerUtils.getSecret(options.getSecretManagerProjectId(), options.getSslKeystorePassSecretId(), "latest"),
      "ssl.keystore.password", options.getSslKeystorePassSecretId(),
      "ssl.endpoint.identification.algorithm", options.getSslEndpointIdentificationAlgorithm()
    );

    p.apply("Read from Kafka", KafkaIO.<String, String>read()
        .withBootstrapServers(options.getKafkaBootstrapServers())
        //.withConsumerFactoryFn(new SslConsumerFactoryFn(consumerConfig))
        .withConsumerConfigUpdates(consumerConfig)
        .withTopic(options.getKafkaTopic())
        .withKeyDeserializerAndCoder(
          StringDeserializer.class, NullableCoder.of(StringUtf8Coder.of()))
        .withValueDeserializerAndCoder(
          StringDeserializer.class, NullableCoder.of(StringUtf8Coder.of())
          )
      .withoutMetadata()
    )
    .apply("Kafka to PubSub", ParDo.of(new DoFn<KV<String, String>, PubsubMessage>() {
      @ProcessElement
      public void processElement(ProcessContext c)  {
        String message = c.element().getValue();
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("timestamp", String.valueOf(System.currentTimeMillis()));

        c.output(new PubsubMessage(payload, attributes));
      }
    })).setCoder(PubsubMessageWithAttributesCoder.of())
    .apply("Publish to PubSub", PubsubIO.writeMessages()
      .to(options.getPubsubTopic())
    );

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    StreamPipelineOptions options = PipelineOptionsFactory
    .fromArgs(args).as(StreamPipelineOptions.class);

    options.setSdkContainerImage(options.getSdkContainerImage());
    options.setExperiments(ImmutableList.of(
      "use_runner_v2",
      "disable_conscrypt_security_provider" 
    ));

    runKafka2Pubsub(options);
  }
}
