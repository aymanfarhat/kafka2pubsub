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

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class SecretManagerHelper {
  public static class DefaultSecretManagerServiceClientHolder {
    public static final SecretManagerServiceClient INSTANCE;

    static {
      try {
        INSTANCE = SecretManagerServiceClient.create();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private final SecretManagerServiceClient client;
  private final String projectId;

  public SecretManagerHelper(SecretManagerServiceClient client, String projectId) {
    this.client = client;
    this.projectId = projectId;
  }

  public ByteString fetchSecretData(String secretName) {
    SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretName, "latest");
    AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder().setName(secretVersionName.toString()).build();

    return client.accessSecretVersion(request).getPayload().getData();
  }

  public void downloadSecretDataToFile(String secretName, String filePath) {
    ByteString secretData = fetchSecretData(secretName);
    try {
      FileUtils.writeByteArrayToFile(new File(filePath), secretData.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String downloadSecretDataToString(String secretName) {
    ByteString secretData = fetchSecretData(secretName);
    return secretData.toStringUtf8();
  }
}
