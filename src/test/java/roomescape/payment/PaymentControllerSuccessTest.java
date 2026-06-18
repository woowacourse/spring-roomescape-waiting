package roomescape.payment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.common.AcceptanceTest;
import roomescape.payment.client.gateway.PaymentGateway;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.payment.service.dto.PaymentStatus;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.slot.fixture.SlotApiFixture.createSlot;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;

class PaymentControllerSuccessTest extends AcceptanceTest {

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    @DisplayName("결제 승인 성공 시 예약이 RESERVED로 확정된다.")
    void confirm_payment_promotes_reservation_to_reserved() {
        String date = LocalDate.of(2099, 1, 1).toString();
        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, "10:00");
        Integer themeId = createTheme(managerToken, "테마1");
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);

        ExtractableResponse<Response> reservationResponse = RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        String orderId = reservationResponse.path("orderId");
        Long amount = ((Number) reservationResponse.path("amount")).longValue();

        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk", orderId, PaymentStatus.DONE, amount));

        RestAssured.given().log().all()
                .queryParam("paymentKey", "test_pk")
                .queryParam("orderId", orderId)
                .queryParam("amount", amount)
                .when().get("/payments/success")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        String reservationStatus = RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .when().get("/member/my-reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getString("[0].status");

        Assertions.assertThat(reservationStatus)
                .isEqualTo(ReservationStatus.RESERVED.name());
    }
}
