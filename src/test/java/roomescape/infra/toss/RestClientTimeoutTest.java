package roomescape.infra.toss;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Disabled("외부 네트워크와 OS 라우팅에 의존하는 timeout 관찰용 테스트")
class RestClientTimeoutTest {

    private static final String BLACK_HOLE_URL = "http://10.255.255.1:81";

    @Test
    void connectTimeout이_없으면_오래_걸린다() {
        RestClient client = RestClient.builder()
                .baseUrl(BLACK_HOLE_URL)
                .build();

        long start = System.currentTimeMillis();
        try {
            client.get().retrieve().body(String.class);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("걸린 시간: " + elapsed + "ms");
            System.out.println("예외: " + e.getClass().getSimpleName());
        }
    }

    @Test
    void connectTimeout을_걸면_빠르게_포기한다() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);

        RestClient client = RestClient.builder()
                .baseUrl(BLACK_HOLE_URL)
                .requestFactory(factory)
                .build();

        long start = System.currentTimeMillis();
        try {
            client.get().retrieve().body(String.class);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("걸린 시간: " + elapsed + "ms");
            System.out.println("예외: " + e.getClass().getSimpleName());
        }
    }
}
