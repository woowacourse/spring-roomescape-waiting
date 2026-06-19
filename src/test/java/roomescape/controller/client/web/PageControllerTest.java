package roomescape.controller.client.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.client.TossConfirmResultUnknownException;
import roomescape.client.TossConnectionException;
import roomescape.client.TossPaymentException;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.service.PaymentService;
import roomescape.service.result.PaymentConfirmResult;

@WebMvcTest(PageController.class)
class PageControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 결제_승인에_성공하면_성공_화면에_예약_정보가_노출된다() {
        TossPaymentResponse response = new TossPaymentResponse(
                "payment-key-1", "order-1", "주문명", "DONE", 30000L, "카드", "2024-01-01T00:00:00");
        PaymentConfirmResult result = new PaymentConfirmResult(
                response, "공포테마", "https://image.com/image.png", LocalDate.of(2024, 1, 2), LocalTime.of(10, 0));
        when(paymentService.confirm(any(), any(), any())).thenReturn(result);

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK)
                .body(containsString("공포테마"));
    }

    @Test
    void 이미_처리된_결제이면_성공이_아닌_안내_화면으로_이동한다() {
        when(paymentService.confirm(any(), any(), any()))
                .thenThrow(new TossPaymentException.AlreadyProcessed("이미 처리된 결제입니다."));

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK)
                .body(containsString("이미 처리된 결제예요"))
                .body(containsString("ALREADY_PROCESSED"));
    }

    @Test
    void 결제_승인_결과를_확인할_수_없으면_확인_불가_안내_화면으로_이동한다() {
        when(paymentService.confirm(any(), any(), any()))
                .thenThrow(new TossConfirmResultUnknownException(new RuntimeException("read timeout")));

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK)
                .body(containsString("결제 결과를 확인하지 못했어요"))
                .body(containsString("CONFIRM_RESULT_UNKNOWN"));
    }

    @Test
    void 토스_서버에_연결할_수_없으면_연결_실패_안내가_노출된다() {
        when(paymentService.confirm(any(), any(), any()))
                .thenThrow(new TossConnectionException(new RuntimeException("connect timeout")));

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK)
                .body(containsString("CONNECTION_FAILED"));
    }

    @Test
    void 예상하지_못한_예외는_일반_결제_실패_화면으로_이동한다() {
        when(paymentService.confirm(any(), any(), any()))
                .thenThrow(new IllegalStateException("알 수 없는 오류"));

        RestAssuredMockMvc.given().spec(defaultSpec())
                .queryParam("paymentKey", "payment-key-1")
                .queryParam("orderId", "order-1")
                .queryParam("amount", 30000L)
                .when().get("/payments/success")
                .then()
                .status(HttpStatus.OK)
                .body(containsString("CONFIRM_FAILED"))
                .body(containsString("알 수 없는 오류"));
    }
}
