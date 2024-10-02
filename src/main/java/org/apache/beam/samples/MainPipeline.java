/**
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.samples;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessageWithAttributesCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.common.serialization.StringDeserializer;

public class MainPipeline {
  static void runKafka2Pubsub(StreamPipelineOptions options) {
    // Initialize the Kafka SSL properties on the worker
    new KafkaSslInitializer().beforeProcessing(options);

    SecretManagerServiceClient secretsClient =
        SecretManagerHelper.DefaultSecretManagerServiceClientHolder.INSTANCE;
    SecretManagerHelper secretManagerHelper =
        new SecretManagerHelper(secretsClient, options.getSecretManagerProjectId());

    Pipeline p = Pipeline.create(options);

    Map<String, Object> consumerConfig =
        ImmutableMap.of(
            "security.protocol", "SSL",
            "ssl.truststore.location", options.getSslTruststoreLocation(),
            "ssl.truststore.password", secretManagerHelper.downloadSecretDataToString(
                    options.getSslTruststorePassSecretId()),
            "ssl.keystore.location", options.getSslKeystoreLocation(),
            "ssl.keystore.password", secretManagerHelper.downloadSecretDataToString(
                    options.getSslKeystorePassSecretId()),
            "ssl.endpoint.identification.algorithm",
                options.getSslEndpointIdentificationAlgorithm());

    p.apply(
            "Read from Kafka",
            KafkaIO.<String, String>read()
                .withBootstrapServers(options.getKafkaBootstrapServers())
                .withConsumerConfigUpdates(consumerConfig)
                .withTopic(options.getKafkaTopic())
                .withKeyDeserializerAndCoder(
                    StringDeserializer.class, NullableCoder.of(StringUtf8Coder.of()))
                .withValueDeserializerAndCoder(
                    StringDeserializer.class, NullableCoder.of(StringUtf8Coder.of()))
                .withoutMetadata())
        .apply(
            "Kafka to PubSub",
            ParDo.of(
                new DoFn<KV<String, String>, PubsubMessage>() {
                  @ProcessElement
                  public void processElement(ProcessContext c) {
                    String message = c.element().getValue();
                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    HashMap<String, String> attributes = new HashMap<String, String>();
                    attributes.put("timestamp", String.valueOf(System.currentTimeMillis()));

                    c.output(new PubsubMessage(payload, attributes));
                  }
                }))
        .setCoder(PubsubMessageWithAttributesCoder.of())
        .apply("Publish to PubSub", PubsubIO.writeMessages().to(options.getPubsubTopic()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    StreamPipelineOptions streamOptions =
        PipelineOptionsFactory.fromArgs(args).as(StreamPipelineOptions.class);

    runKafka2Pubsub(streamOptions);
  }
}
