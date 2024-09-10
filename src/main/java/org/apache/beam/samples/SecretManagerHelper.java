package org.apache.beam.samples;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.protobuf.ByteString;


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
    AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
        .setName(secretVersionName.toString())
        .build();

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
