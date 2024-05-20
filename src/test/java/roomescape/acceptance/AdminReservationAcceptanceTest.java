package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.dto.ErrorResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.request.AdminReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;

public class AdminReservationAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("관리자 권한으로 예약을 추가한다.")
    void createReservation() {
        // given
        Member admin = createTestAdmin();
        String token = createTestToken(admin);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();
        Long memberID = createTestMember().getId();

        AdminReservationSaveRequest request = new AdminReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, memberID);
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .extract();
        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(reservationResponse.id()).isNotNull();
            softly.assertThat(reservationResponse.memberName()).isEqualTo(MIA_NAME);
        });
    }

    @Test
    @DisplayName("사용자가 관리자 예약 추가 기능을 사용한다.")
    void createReservationWithoutAuthority() {
        // given
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();
        Member member = createTestMember();
        String token = createTestToken(member);

        AdminReservationSaveRequest request = new AdminReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, member.getId());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusUnauthorized(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }
}
