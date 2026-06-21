package roomescape.controller.client.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.domain.PaymentResult;
import roomescape.service.PaymentService;
import roomescape.service.result.PaymentConfirmResult;

@WebMvcTest(PageController.class)
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001"
})
class RateLimitInterceptorTest extends BaseControllerUnitTest {

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 한도내_요청은_200이고_한도초과_요청은_429와_RetryAfter헤더를_받는다() {
        PaymentResult response = new PaymentResult("order-1", "DONE", 30000L, "2024-01-01T00:00:00");
        PaymentConfirmResult result = new PaymentConfirmResult(
                response, "공포테마", "https://image.com/image.png", LocalDate.of(2024, 1, 2), LocalTime.of(10, 0));
        when(paymentService.confirm(any(), any(), any())).thenReturn(result);

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK);

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-2")
                .queryParam("orderId", "order-2")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", org.hamcrest.Matchers.notNullValue());
    }
}
