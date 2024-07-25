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
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Description;


public interface StreamPipelineOptions extends PipelineOptions {
    @Description("Kafka topic to read from")
    String getKafkaTopic();

    void setKafkaTopic(String value);

    @Description("PubSub topic to publish to")
    String getPubsubTopic();

    void setPubsubTopic(String value);

    @Description("Kafka bootstrap servers")
    String getKafkaBootstrapServers();

    void setKafkaBootstrapServers(String value);

    @Description("SSL Truststore location on the local worker")
    String getSslTruststoreLocation();

    void setSslTruststoreLocation(String value);

    @Description("SSL Truststore password secret Id")
    String getSslTruststorePassSecretId();

    void setSslTruststorePassSecretId(String value);

    @Description("SSL Keystore location on the local worker")
    String getSslKeystoreLocation();

    void setSslKeystoreLocation(String value);

    @Description("SSL Keystore password secret Id")
    String getSslKeystorePassSecretId();

    void setSslKeystorePassSecretId(String value);

    @Description("GCP project ID for the secrets store")
    String getSecretManagerProjectId();

    void setSecretManagerProjectId(String value);

    @Description("SSL endpoint identification algorithm for Kafka")
    String getSslEndpointIdentificationAlgorithm();

    void setSslEndpointIdentificationAlgorithm(String value);
}