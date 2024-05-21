package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.reservation.AdminReservationSaveRequest;
import roomescape.dto.reservation.MemberReservationSaveRequest;

import static roomescape.TestFixture.*;

class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("사용자가 예약을 성공적으로 생성하면 201을 응답한다.")
    void respondCreatedWhenCreateReservation() {
        final Long timeId = saveReservationTime();
        final Long themeId = saveTheme();
        final MemberReservationSaveRequest request
                = new MemberReservationSaveRequest(DATE_MAY_EIGHTH, timeId, themeId, "RESERVED");

        assertCreateResponseWithToken(request, MEMBER_MIA_EMAIL, "/reservations", 201);
    }

    @Test
    @DisplayName("관리자가 예약을 성공적으로 생성하면 201을 응답한다.")
    void respondCreatedWhenAdminCreateReservation() {
        final Long timeId = saveReservationTime();
        final Long themeId = saveTheme();
        final AdminReservationSaveRequest request = new AdminReservationSaveRequest(1L, DATE_MAY_EIGHTH, timeId, themeId);

        assertCreateResponseWithToken(request, ADMIN_EMAIL, "/admin/reservations", 201);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간으로 예약 생성 시 400을 응답한다.")
    void respondBadRequestWhenNotExistingReservationTime() {
        saveReservationTime();
        final Long themeId = saveTheme();
        final MemberReservationSaveRequest request
                = new MemberReservationSaveRequest(DATE_MAY_EIGHTH, 0L, themeId, "RESERVED");

        assertCreateResponseWithToken(request, MEMBER_MIA_EMAIL, "/reservations", 400);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약 생성 시 400을 응답한다.")
    void respondBadRequestWhenNotExistingTheme() {
        saveTheme();
        final Long timeId = saveReservationTime();
        final MemberReservationSaveRequest request
                = new MemberReservationSaveRequest(DATE_MAY_EIGHTH, timeId, 0L, "RESERVED");

        assertCreateResponseWithToken(request, MEMBER_MIA_EMAIL, "/reservations", 400);
    }

    @Test
    @DisplayName("예약 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindReservations() {
        saveReservation();
        
        assertGetResponse("/reservations", 200);
    }
    
    @Test
    @DisplayName("테마, 사용자, 예약 날짜로 예약 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFilteredFindReservations() {
        saveReservation();
        final String accessToken = getAccessToken(MEMBER_MIA_EMAIL);

        RestAssured.given().log().all()
                .queryParam("themeId", 1L)
                .queryParam("memberId", 1L)
                .queryParam("dateFrom", "2034-05-01")
                .queryParam("dateTo", "R2034-05-08")
                .cookie("token", accessToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }
    
    @Test
    @DisplayName("예약을 성공적으로 삭제하면 204를 응답한다.")
    void respondNoContentWhenDeleteReservation() {
        final Long reservationId = saveReservation();

        assertDeleteResponse("/reservations/", reservationId, 204);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 400을 응답한다.")
    void respondBadRequestWhenDeleteNotExistingReservation() {
        saveReservation();
        final Long notExistingReservationTimeId = 0L;

        assertDeleteResponse("/reservations/", notExistingReservationTimeId, 400);
    }

    @Test
    @DisplayName("특정 사용자의 예약 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindMyReservations() {
        saveReservation();

        assertGetResponseWithLogin(MEMBER_MIA_EMAIL, "/reservations/mine", 200);
    }
    
    @Test
    @DisplayName("예약 대기 목록을 성공적으로 조회하면 200을 응답한다.")
    void respondOkWhenFindReservationWaitings() {
        saveReservationWaiting();

        assertGetResponseWithLogin(ADMIN_EMAIL, "/admin/reservations", 200);
    }

    @Test
    @DisplayName("예약 대기를 성공적으로 승인하면 200을 응답한다.")
    void respondOkWhenApproveReservationWaiting() {
        final Long id = saveReservationWaiting();
        final String accessToken = getAccessToken(ADMIN_EMAIL);

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().put("/admin/reservations/waiting/" + id)
                .then().log().all()
                .statusCode(200);
    }
}
