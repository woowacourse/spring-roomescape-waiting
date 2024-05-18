package roomescape.acceptance.admin;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.acceptance.BaseAcceptanceTest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance.Fixture.adminToken;
import static roomescape.acceptance.PreInsertedData.*;

@DisplayName("관리자가 예약을 필터링해서 조회한다.")
class ReservationFilterAcceptanceTest extends BaseAcceptanceTest {

    @DisplayName("필터링하지 않음")
    @Test
    void filterNone() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200),
                ReservationResponse.from(RESERVATION_CUSTOMER2_THEME3_240502_1200)
        );
    }

    @DisplayName("사용자로 필터링")
    @Test
    void filterByMemberId() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("?memberId=2");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200)
        );
    }

    @DisplayName("테마로 필터링")
    @Test
    void filterByThemeId() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("?themeId=2");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200)
        );
    }

    @Test
    @DisplayName("시작 날짜로 필터링")
    void filterByDateFrom() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("?dateFrom=2024-05-02");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER2_THEME3_240502_1200)
        );
    }

    @DisplayName("끝 날짜로 필터링")
    @Test
    void filterByTimeId() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("?dateTo=2024-05-01");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1100),
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME2_240501_1200)
        );
    }

    @DisplayName("종합 필터링")
    @Test
    void filterComprehensive() {
        MultipleResponse<ReservationResponse> response = sendRequestFiltering("?memberId=2&themeId=3&dateFrom=2024-05-02&dateTo=2024-05-02");

        assertThat(response.items()).containsExactlyInAnyOrder(
                ReservationResponse.from(RESERVATION_CUSTOMER1_THEME3_240502_1100)
        );
    }

    private MultipleResponse<ReservationResponse> sendRequestFiltering(String path) {
        TypeRef<MultipleResponse<ReservationResponse>> memberListFormat = new TypeRef<>() {
        };

        return RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin/reservations/filter" + path)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(memberListFormat);
    }
}
