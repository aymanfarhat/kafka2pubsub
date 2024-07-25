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

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.protobuf.ByteString;


public class SecretManagerUtils {
   public static String getSecret(String projectId, String secretId, String versionId) {
      try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
        SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
        AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
            .setName(secretVersionName.toString())
            .build();

        ByteString payload = client.accessSecretVersion(request).getPayload().getData();
        String secretValue = payload.toStringUtf8();

        return secretValue;
      } catch (Exception e) {
         throw new RuntimeException("Error retrieving secret", e);
      }
   } 
}
