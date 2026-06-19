package roomescape.controller.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import roomescape.controller.BaseControllerUnitTest;
import roomescape.controller.client.dto.request.ReservationRequest;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
import roomescape.controller.client.fixture.ReservationApiRequestFixture;
import roomescape.service.PaymentService;
import roomescape.service.command.ReservationCommand;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest extends BaseControllerUnitTest {

    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvcSetting(webApplicationContext);
    }

    @Test
    void 결제_준비_요청에_성공하면_200_OK와_응답이_반환된다() {
        // given
        ReservationRequest body = ReservationApiRequestFixture.reserveSuccessRequestFixture();
        PreparePaymentResponse serviceResponse = new PreparePaymentResponse(
                "test-order-id", 30000L, "공포테마 (2025-01-02 10:00)", "test_ck_placeholder"
        );
        when(paymentService.prepare(any(ReservationCommand.class))).thenReturn(serviceResponse);

        // when & then
        PreparePaymentResponse response = RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/payments/prepare")
                .then().log().all()
                .status(HttpStatus.OK)
                .extract().as(new TypeRef<>() {});

        assertThat(response).isEqualTo(serviceResponse);
    }

    @ParameterizedTest(name = "요청 정보가 {0} 일 때, 예외 메세지 \"{1}\"가 발생한다.")
    @MethodSource("roomescape.controller.client.fixture.ReservationApiRequestFixture#reserveFailRequestFixture")
    void 결제_준비_요청_시_형식_검증에_실패하면_400이_반환된다(ReservationRequest body, String exceptionMessage) {
        RestAssuredMockMvc.given().spec(defaultSpec()).log().all()
                .body(body)
                .when().post("/api/payments/prepare")
                .then().log().all()
                .status(HttpStatus.BAD_REQUEST)
                .body(containsString(exceptionMessage));
    }
}
