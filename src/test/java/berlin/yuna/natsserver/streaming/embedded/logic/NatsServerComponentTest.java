package berlin.yuna.natsserver.streaming.embedded.logic;

import berlin.yuna.natsserver.streaming.embedded.annotation.EnableNatsStreamingServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

import static berlin.yuna.natsserver.config.NatsStreamingOptions.natsStreamingBuilder;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@EnableNatsStreamingServer
@Tag("IntegrationTest")
@DisplayName("NatsServerComponentTest")
class NatsServerComponentTest {

    @Autowired
    private NatsStreamingServer natsServer;

    @Test
    @DisplayName("Download and start server")
    void natsServer_shouldDownloadUnzipAndStart() throws IOException {
        Files.deleteIfExists(natsServer.binary());
        assertThat(natsServer, is(notNullValue()));
        assertThat(natsServer.port(), is(4222));
        assertThat(natsServer.pid(), is(greaterThan(-1)));
    }

    @Test
    @DisplayName("Port config with double dash")
    void secondNatsServer_withDoubleDotSeparatedProperty_shouldStartSuccessful() {
        assertNatsServerStart(4225, "--port", "4225");
    }

    @Test
    @DisplayName("Port config without dashes")
    void secondNatsServer_withOutMinusProperty_shouldStartSuccessful() {
        assertNatsServerStart(4226, "port", "4226");
    }

    @Test
    @DisplayName("Invalid config [FAIL]")
    void secondNatsServer_withInvalidProperty_shouldFailToStart() {
        assertThrows(
                IllegalArgumentException.class,
                () -> assertNatsServerStart(4228, "p", "4228"),
                "No enum constant"
        );
    }

    @Test
    @DisplayName("ToString")
    void toString_shouldPrintPortAndOs() {
        final String serverString = natsServer.toString();
        assertThat(serverString, containsString("4222"));
    }

    private void assertNatsServerStart(final int port, final String... config) {
        try (final NatsStreamingServer natsServer = new NatsStreamingServer(natsStreamingBuilder().timeoutMs(10000).config(config).build())) {
            new Socket("localhost", port).close();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
