package roomescape.infrastructure.toss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
class TossPaymentConfigTest {

    @Autowired
    @Qualifier("tossClientHttpRequestFactory")
    private ClientHttpRequestFactory requestFactory;

    @Test
    @DisplayName("토스 RestClient 요청 팩토리에 application.yml의 connect/read timeout을 설정한다")
    void tossRestClientTimeout() {
        assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);
        assertThat(ReflectionTestUtils.getField(requestFactory, "connectTimeout")).isEqualTo(2_000);
        assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout")).isEqualTo(3_000);
    }
}
