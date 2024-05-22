package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationSaveRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.*;

public class WaitingAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("예약 대기를 성공적으로 등록하면 201을 응답한다.")
    void respondOkWhenCreateReservationWaiting() {
        final Long timeId = saveReservationTime();
        final Long themeId = saveTheme();
        final ReservationSaveRequest request
                = new ReservationSaveRequest(null, DATE_MAY_EIGHTH, timeId, themeId, "WAITING");

        final ReservationResponse response = assertPostResponseWithToken(request, MEMBER_MIA_EMAIL, "/waitings", 201)
                .extract().as(ReservationResponse.class);

        assertAll(() -> {
            assertThat(response.name()).isEqualTo(MEMBER_MIA_NAME);
            assertThat(response.status()).isEqualTo(ReservationStatus.WAITING);
        });
    }

    @Test
    @DisplayName("예약 대기 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindReservationWaitings() {
        saveReservationWaiting();
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        final JsonPath jsonPath = assertGetResponseWithToken(accessToken, "/admin/waitings", 200)
                .extract().response().jsonPath();

        assertAll(() -> {
            assertThat(jsonPath.getString("name[0]")).isEqualTo(MEMBER_MIA_NAME);
            assertThat(jsonPath.getString("status[0]")).isEqualTo(ReservationStatus.WAITING.name());
        });
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 승인하면 200을 응답한다.")
    void respondOkWhenApproveReservationWaiting() {
        final Long id = saveReservationWaiting();
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().put("/admin/waitings/" + id)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 거절하면 204를 응답한다.")
    void responseNoContentWhenRejectReservationWaiting() {
        final Long waitingId = saveReservationWaiting();
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().delete("/admin/waitings/" + waitingId)
                .then().log().all()
                .statusCode(204);
    }
}
