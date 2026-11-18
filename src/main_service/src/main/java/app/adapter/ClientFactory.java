package app.adapter;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
public class ClientFactory {
    @Value("${rest.client.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${rest.client.read-timeout:5000}")
    private int readTimeout;

    public RestClient getRestClient(String baseUri) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUri)
                .requestFactory(factory)
                .build();
    }
}
