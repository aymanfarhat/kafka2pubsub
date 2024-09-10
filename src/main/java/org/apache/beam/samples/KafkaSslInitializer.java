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

import org.apache.beam.sdk.harness.JvmInitializer;
import com.google.auto.service.AutoService;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import org.apache.beam.sdk.options.PipelineOptions;


@AutoService(JvmInitializer.class)
public class KafkaSslInitializer implements JvmInitializer {
  @Override
  public void beforeProcessing(PipelineOptions options) {
    StreamPipelineOptions streamOptions = options.as(StreamPipelineOptions.class);

    SecretManagerServiceClient client = SecretManagerHelper.DefaultSecretManagerServiceClientHolder.INSTANCE;
    SecretManagerHelper secretManagerHelper = new SecretManagerHelper(client, streamOptions.getSecretManagerProjectId());

    secretManagerHelper.downloadSecretDataToFile(streamOptions.getSslTruststoreFileSecretId(), "/tmp/client.truststore.jks");
    secretManagerHelper.downloadSecretDataToFile(streamOptions.getSslKeystoreFileSecretId(), "/tmp/server.keystore.jks");
  }
}