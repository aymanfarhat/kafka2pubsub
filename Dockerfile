FROM apache/beam_java11_sdk:2.57.0

# Copy the secrets folder to the container
# This folder should contain client.truststore.jks and client.keystore.jks
COPY ./secrets /secrets