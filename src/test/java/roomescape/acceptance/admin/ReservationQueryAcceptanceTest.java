package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.*;

class ReservationQueryAcceptanceTest extends BaseAcceptanceTest {

    List<ReservationResponse> reserved = List.of(
            ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
            ReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100),
            ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200),
            ReservationResponse.from(RESERVATION_CUSTOMER2_THEME3_240502_1200),
            ReservationResponse.from(RESERVATION_CUSTOMER2_THEME3_240503_1200)
    );
    List<ReservationResponse> waiting = List.of(
            ReservationResponse.from(RESERVATION_WAITING_CUSTOMER1_THEME3_240502_1200),
            ReservationResponse.from(RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100)
    );

    @DisplayName("관리자가 예약된 예약 목록을 조회한다.")
    @Test
    void getReservedReservations_success() {
        TypeRef<MultipleResponse<ReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        MultipleResponse<ReservationResponse> response = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(reservationListFormat);

        assertAll(
                () -> assertThat(response.items()).containsExactlyInAnyOrderElementsOf(reserved),
                () -> assertThat(response.items()).doesNotContainAnyElementsOf(waiting)
        );
    }

    @DisplayName("관리자가 예약 대기중인 예약 목록을 조회한다.")
    @Test
    void getWaitingReservations_success() {
        TypeRef<MultipleResponse<ReservationResponse>> reservationListFormat = new TypeRef<>() {
        };

        MultipleResponse<ReservationResponse> response = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(reservationListFormat);

        assertAll(
                () -> assertThat(response.items()).containsExactlyInAnyOrderElementsOf(waiting),
                () -> assertThat(response.items()).doesNotContainAnyElementsOf(reserved)
        );
    }
}
