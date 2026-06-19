package roomescape.payment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import roomescape.common.AcceptanceTest;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.date.fixture.ReservationDateApiFixture.createReservationDate;
import static roomescape.slot.fixture.SlotApiFixture.createSlot;
import static roomescape.theme.fixture.ThemeApiFixture.createTheme;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;

class PaymentControllerFailureTest extends AcceptanceTest {

    @Test
    @DisplayName("결제 실패 시 PENDING_PAYMENT 상태의 예약을 취소 처리한다.")
    void cancel_pending_reservation_on_payment_fail() {
        // given
        String date = LocalDate.of(2099, 1, 1).toString();
        String startAt = "10:00";
        String themeName = "테마1";

        Integer dateId = createReservationDate(managerToken, date);
        Integer timeId = createReservationTime(managerToken, startAt);
        Integer themeId = createTheme(managerToken, themeName);
        Integer slotId = createSlot(managerToken, dateId, timeId, themeId);

        // 사용자가 예약을 시도하여 PENDING_PAYMENT 상태가 됨
        ExtractableResponse<Response> reservationResponse = RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        String orderId = reservationResponse.path("orderId");
        assertThat(orderId).isNotNull();
        assertThat(reservationResponse.path("status").toString()).isEqualTo(ReservationStatus.PENDING_PAYMENT.name());

        // when: 결제 실패(/payments/fail) 호출
        RestAssured.given().log().all()
                .queryParam("orderId", orderId)
                .queryParam("code", "USER_CANCEL")
                .queryParam("message", "결제 취소")
                .when().get("/payments/fail")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then: 동일한 슬롯에 다시 예약이 가능해야 함 (이전 예약이 취소되었으므로 WAITING이 아닌 PENDING_PAYMENT가 되어야 함)
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, memberToken)
                .contentType(ContentType.JSON)
                .when().post("/member/slots/" + slotId + "/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("status", is(ReservationStatus.PENDING_PAYMENT.name()));
    }

}
