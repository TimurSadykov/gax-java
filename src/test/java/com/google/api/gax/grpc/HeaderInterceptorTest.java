package com.google.api.gax.grpc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptors;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Tests for {@link HeaderInterceptor}.
 */
@RunWith(JUnit4.class)
public class HeaderInterceptorTest {

  @Mock
  private Channel channel;

  @Mock
  private ClientCall<String, Integer> call;

  @Mock
  private MethodDescriptor<String, Integer> method;

  /**
   * Sets up mocks.
   */
  @Before public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(channel.newCall(
        Mockito.<MethodDescriptor<String, Integer>>any(), any(CallOptions.class)))
        .thenReturn(call);
  }

  @Test
  public void testInterceptor() {
    final Metadata.Key<String> headerKey =
        Metadata.Key.of("x-google-apis-agent", Metadata.ASCII_STRING_MARSHALLER);
    String data = "abcd";
    HeaderInterceptor interceptor = new HeaderInterceptor(data);
    Channel intercepted = ClientInterceptors.intercept(channel, interceptor);
    @SuppressWarnings("unchecked")
    ClientCall.Listener<Integer> listener = mock(ClientCall.Listener.class);
    ClientCall<String, Integer> interceptedCall = intercepted.newCall(method, CallOptions.DEFAULT);
    // start() on the intercepted call will eventually reach the call created by the real channel
    interceptedCall.start(listener, new Metadata());
    // The headers passed to the real channel call will contain the information inserted by the
    // interceptor.
    ArgumentCaptor<Metadata> captor = ArgumentCaptor.forClass(Metadata.class);
    verify(call).start(same(listener), captor.capture());
    assertEquals(data, captor.getValue().get(headerKey));
  }
}
