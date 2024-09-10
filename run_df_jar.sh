java -cp target/kafka2pubsub-1.0-SNAPSHOT.jar  \
    org.apache.beam.samples.MainPipeline \
    --jobName="kafka-to-pubsub-`date +%Y%m%d-%H%M%S`" \
    --runner="DataflowRunner" \
    --project="$PROJECT" \
    --gcpTempLocation="gs://$PIPELINE_GCS_BUCKET/tmp/" \
    --stagingLocation="gs://$PIPELINE_GCS_BUCKET/staging/" \
    --defaultWorkerLogLevel="DEBUG" \
    --region="$REGION" \
    --kafkaBootstrapServers="$KAFKA_BOOTSTRAP_SERVERS" \
    --kafkaTopic="$KAFKA_TOPIC" \
    --pubsubTopic="$PUBSUB_TOPIC" \
    --sslTruststoreFileSecretId="$SSL_TRUSTSTORE_FILE_SECRET_ID" \
    --sslTruststoreLocation="$SSL_TRUSTSTORE_LOCATION" \
    --sslTruststorePassSecretId="$SSL_TRUSTSTORE_PASS_SECRET_ID" \
    --sslKeystoreFileSecretId="$SSL_KEYSTORE_FILE_SECRET_ID" \
    --sslKeystoreLocation="$SSL_KEYSTORE_LOCATION" \
    --sslKeystorePassSecretId="$SSL_KEYSTORE_PASS_SECRET_ID" \
    --secretManagerProjectId="$SECRET_MANAGER_PROJECT_ID" \
    --sslEndpointIdentificationAlgorithm="" \
    --experiments="use_runner_v2" \
    --enableStreamingEngine \
    --numWorkers=1 \
    --maxNumWorkers=1 \
    --workerMachineType="n1-standard-1" \
    --diskSizeGb=10 \
    --autoscalingAlgorithm="NONE" \
    --network=$NETWORK \
    --subnetwork=$SUBNETWORK

