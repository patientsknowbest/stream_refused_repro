package io.pkb.foo;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClient;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.client.api.Header;
import ca.uhn.fhir.rest.client.api.IHttpClient;
import ca.uhn.fhir.rest.client.impl.RestfulClientFactory;
import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MyBadHttpRestfulClientFactory extends RestfulClientFactory {

    private Call.Factory myNativeClient;

    public MyBadHttpRestfulClientFactory(OkHttpClient client, FhirContext theFhirContext) {
        super(theFhirContext);
        myNativeClient = client;
    }

    @Override
    protected IHttpClient getHttpClient(String theServerBase) {
        return new OkHttpRestfulClient(myNativeClient, new StringBuilder(theServerBase), null, null, null, null);
    }

    @Override
    protected void resetHttpClient() {
        myNativeClient = null;
    }

    @Override
    public IHttpClient getHttpClient(StringBuilder theUrl,
                                     Map<String, List<String>> theIfNoneExistParams,
                                     String theIfNoneExistString,
                                     RequestTypeEnum theRequestType,
                                     List<Header> theHeaders) {
        return new OkHttpRestfulClient(myNativeClient, theUrl, theIfNoneExistParams, theIfNoneExistString, theRequestType, theHeaders);
    }

    /**
     * Only accepts clients of type {@link OkHttpClient}
     *
     * @param okHttpClient
     */
    @Override
    public void setHttpClient(Object okHttpClient) {
        myNativeClient = (Call.Factory) okHttpClient;
    }

    @Override
    public void setProxy(String theHost, Integer thePort) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(theHost, thePort));
        OkHttpClient.Builder builder = ((OkHttpClient)myNativeClient).newBuilder().proxy(proxy);
        setHttpClient(builder.build());
    }

}
