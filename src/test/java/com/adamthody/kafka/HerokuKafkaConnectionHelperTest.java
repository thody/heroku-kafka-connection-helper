package com.adamthody.kafka;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static com.adamthody.kafka.HerokuKafkaConnectionHelper.ConnectionConfigs.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HerokuKafkaConnectionHelperTest {

  @Rule
  public final EnvironmentVariables envVars = new EnvironmentVariables();

  @Test
  public void sslConfiguration() throws Exception {
    envVars.set("KAFKA_URL", "kafka+ssl://1.1.1.1:1,kafka+ssl://2.2.2.2:2,kafka+ssl://3.3.3.3:3");
    envVars.set("KAFKA_TRUSTED_CERT", getFileContents("test.cert"));
    envVars.set("KAFKA_CLIENT_CERT", getFileContents("test.cert"));
    envVars.set("KAFKA_CLIENT_CERT_KEY", getFileContents("test.key"));

    Properties props = HerokuKafkaConnectionHelper.getConfigProperties();

    assertThat(props.getProperty(SECURITY_PROTOCOL_CONFIG), equalTo("SSL"));
    assertThat(props.getProperty(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_TYPE_CONFIG));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_LOCATION_CONFIG));
    assertNotNull(props.getProperty(SSL_TRUSTSTORE_PASSWORD_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_TYPE_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_LOCATION_CONFIG));
    assertNotNull(props.getProperty(SSL_KEYSTORE_PASSWORD_CONFIG));
  }

  @Test
  public void missingKafkaUrlEnvVar() throws Exception {
    try {
      HerokuKafkaConnectionHelper.getConfigProperties();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage(), containsString("KAFKA_URL"));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void missingSSLEnvironmentVariables() throws Exception {
    envVars.set("KAFKA_URL", "kafka+ssl://1.1.1.1:1,kafka+ssl://2.2.2.2:2,kafka+ssl://3.3.3.3:3");
    HerokuKafkaConnectionHelper.getConfigProperties();
  }

  @Test
  public void plaintextConfiguration() throws Exception {
    envVars.set("KAFKA_URL", "kafka://1.1.1.1:1,kafka://2.2.2.2:2,kafka://3.3.3.3:3");

    Properties props = HerokuKafkaConnectionHelper.getConfigProperties();

    assertThat(props.getProperty(SECURITY_PROTOCOL_CONFIG), equalTo("PLAINTEXT"));
    assertThat(props.getProperty(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
  }

  @Test
  public void configurationAsMap() throws Exception {
    envVars.set("KAFKA_URL", "kafka://1.1.1.1:1,kafka://2.2.2.2:2,kafka://3.3.3.3:3");

    Map<String, Object> props = HerokuKafkaConnectionHelper.getConfigMap();

    assertThat(props.get(SECURITY_PROTOCOL_CONFIG), equalTo("PLAINTEXT"));
    assertThat(props.get(BOOTSTRAP_SERVERS_CONFIG), equalTo("1.1.1.1:1,2.2.2.2:2,3.3.3.3:3"));
  }

  private String getFileContents(String filename) throws URISyntaxException, IOException {
    Path filePath = Paths.get(getClass().getResource("/" + filename).toURI());
    return new String(Files.readAllBytes(filePath));
  }
}