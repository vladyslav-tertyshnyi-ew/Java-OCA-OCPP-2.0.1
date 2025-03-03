package eu.chargetime.ocpp;
/*
 ChargeTime.eu - Java-OCA-OCPP
 Copyright (C) 2015-2016 Thomas Volden <tv@chargetime.eu>

 MIT License

 Copyright (C) 2016-2018 Thomas Volden

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
*/

import eu.chargetime.ocpp.wss.WssSocketBuilder;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Web Socket implementation of the Transmitter. */
public class WebSocketTransmitter implements Transmitter {
  private static final Logger logger = LoggerFactory.getLogger(WebSocketTransmitter.class);

  public static final String WSS_SCHEME = "wss";
  private final Draft draft;

  private final JSONConfiguration configuration;
  private volatile boolean closed = true;
  private volatile WebSocketClient client;
  private WssSocketBuilder wssSocketBuilder;

  public WebSocketTransmitter(JSONConfiguration configuration, Draft draft) {
    this.configuration = configuration;
    this.draft = draft;
  }

  public WebSocketTransmitter(Draft draft) {
    this(JSONConfiguration.get(), draft);
  }

  @Override
  public void connect(String uri, RadioEvents events) {
    final URI resource = URI.create(uri);

    Map<String, String> httpHeaders = new HashMap<>();
    String username = configuration.getParameter(JSONConfiguration.USERNAME_PARAMETER);
    Object password = configuration.getParameter(JSONConfiguration.PASSWORD_PARAMETER);
    byte[] credentials = null;
    if (username != null && password != null) {
      if (password instanceof String) {
        credentials = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
      } else if (password instanceof byte[]) {
        credentials =
            joinByteArrays((username + ":").getBytes(StandardCharsets.UTF_8), (byte[]) password);
      }
    }
    if (credentials != null) {
      byte[] base64Credentials = Base64.getEncoder().encode(credentials);
      httpHeaders.put("Authorization", "Basic " + new String(base64Credentials));
    }

    int connectTimeout =
        this.configuration.getParameter(JSONConfiguration.CONNECT_TIMEOUT_IN_MS_PARAMETER, 0);

    client =
        new WebSocketClient(resource, draft, httpHeaders, connectTimeout) {
          @Override
          public void onOpen(ServerHandshake serverHandshake) {
            logger.debug("On connection open (HTTP status: {})", serverHandshake.getHttpStatus());
            events.connected();
          }

          @Override
          public void onMessage(String message) {
            events.receivedMessage(message);
          }

          @Override
          public void onClose(int code, String reason, boolean remote) {
            logger.debug(
                "On connection close (code: {}, reason: {}, remote: {})", code, reason, remote);

            events.disconnected();
          }

          @Override
          public void onError(Exception ex) {
            if (ex instanceof ConnectException) {
              logger.error("On error triggered caused by:", ex);
            } else {
              logger.error("On error triggered:", ex);
            }
          }
        };

    if (WSS_SCHEME.equals(resource.getScheme())) {

      if (wssSocketBuilder == null) {
        throw new IllegalStateException(
            "wssSocketBuilder must be set to support " + WSS_SCHEME + " scheme");
      }

      try {
        client.setSocket(wssSocketBuilder.uri(resource).build());
      } catch (IOException ex) {
        logger.error("SSL socket creation failed", ex);
      }
    }

    configure();

    logger.debug("Trying to connect to: {}", resource);

    try {
      client.connectBlocking();
      closed = false;
    } catch (Exception ex) {
      logger.warn("client.connectBlocking() failed", ex);
    }
  }

  byte[] joinByteArrays(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  void configure() {
    if (client == null) {
      return;
    }
    client.setReuseAddr(configuration.getParameter(JSONConfiguration.REUSE_ADDR_PARAMETER, false));
    client.setTcpNoDelay(
        configuration.getParameter(JSONConfiguration.TCP_NO_DELAY_PARAMETER, false));
    client.setConnectionLostTimeout(
        configuration.getParameter(JSONConfiguration.PING_INTERVAL_PARAMETER, 60));
    client.setProxy(configuration.getParameter(JSONConfiguration.PROXY_PARAMETER, Proxy.NO_PROXY));
  }

  void enableWSS(WssSocketBuilder wssSocketBuilder) {
    if (client != null) {
      throw new IllegalStateException("Cannot enable WSS on already connected client");
    }
    this.wssSocketBuilder = wssSocketBuilder;
  }

  @Override
  public void disconnect() {
    if (client == null) {
      return;
    }
    try {
      client.closeBlocking();
    } catch (Exception ex) {
      logger.info("client.closeBlocking() failed", ex);
    } finally {
      client = null;
      closed = true;
    }
  }

  @Override
  public void send(Object request) throws NotConnectedException {
    if (client == null) {
      throw new NotConnectedException();
    }

    try {
      client.send(request.toString());
    } catch (WebsocketNotConnectedException ex) {
      throw new NotConnectedException();
    }
  }

  public boolean isClosed() {
    return closed;
  }
}
