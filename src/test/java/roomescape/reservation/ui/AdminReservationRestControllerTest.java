package roomescape.reservation.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.ui.LoginApiFixture.adminLoginAndGetCookies;
import static roomescape.fixture.ui.LoginApiFixture.memberLoginAndGetCookies;
import static roomescape.fixture.ui.MemberApiFixture.signUpMembers;
import static roomescape.fixture.ui.MemberApiFixture.signUpRequest1;
import static roomescape.fixture.ui.ReservationTimeApiFixture.createReservationTimes;
import static roomescape.fixture.ui.ThemeApiFixture.createThemes;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.ui.dto.LoginRequest;
import roomescape.fixture.ui.LoginApiFixture;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.SignUpRequest;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.ui.dto.request.CreateBookedReservationRequest;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;
import roomescape.theme.ui.dto.ThemeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
@DisplayName("관리자 예약 관리 API 테스트")
class AdminReservationRestControllerTest {

    private final LocalDate date = LocalDate.now().plusDays(1);
    private List<ReservationTimeResponse> createReservationTimeResponses;
    private List<ThemeResponse> createThemeResponses;
    private List<MemberResponse> createMemberResponses;

    @BeforeEach
    void setUp() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        // 관리자 권한으로 예약 시간 추가 (3개)
        createReservationTimeResponses = createReservationTimes(adminCookies, 3);
        // 관리자 권한으로 테마 추가 (2개)
        createThemeResponses = createThemes(adminCookies, 2);
        // 회원 추가 (2명)
        createMemberResponses = signUpMembers(2);
    }

    @Test
    void 예약을_추가한다() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        final CreateBookedReservationRequest createBookedReservationRequest = bookedReservationRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .body(createBookedReservationRequest)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 관리자_권한이_아니면_예약을_추가할_수_없다() {
        final CreateBookedReservationRequest createBookedReservationRequest = bookedReservationRequest1();

        // public 권한
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(createBookedReservationRequest)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // 회원 권한
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(createBookedReservationRequest)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 예약을_삭제한다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        final CreateBookedReservationRequest createBookedReservationRequest = bookedReservationRequest1();

        // member 예약 추가
        final Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(createBookedReservationRequest)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        // admin이 member 예약 삭제
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().delete("/admin/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 관리자_권한이_아니면_예약을_삭제_할_수_없다() {
        final CreateBookedReservationRequest createBookedReservationRequest = bookedReservationRequest1();
        final Map<String, String> adminCookies = adminLoginAndGetCookies();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .body(createBookedReservationRequest)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        // public 권한
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        // 회원 권한
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(createBookedReservationRequest)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 예약_목록을_조회한다() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .body(bookedReservationRequest1())
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .body(bookedReservationRequest2())
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    @Test
    void 예약_상태_목록을_조회한다() {
        final Map<String, String> adminCookies = LoginApiFixture.adminLoginAndGetCookies();

        final List<ReservationStatusResponse> responses =
                RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .cookies(adminCookies)
                        .when().get("/admin/reservations/statuses")
                        .then().log().all()
                        .extract().jsonPath()
                        .getList(".", ReservationStatusResponse.class);

        assertThat(responses).hasSize(ReservationStatus.values().length);
    }

    private CreateBookedReservationRequest bookedReservationRequest1() {
        final Long memberId = createMemberResponses.get(0).id();
        final Long timeId = createReservationTimeResponses.get(0).id();
        final Long themeId = createThemeResponses.get(0).id();

        return new CreateBookedReservationRequest(memberId, date, timeId, themeId);
    }

    private CreateBookedReservationRequest bookedReservationRequest2() {
        final Long memberId = createMemberResponses.get(0).id();
        final Long timeId = createReservationTimeResponses.get(0).id();
        final Long themeId = createThemeResponses.get(1).id();

        return new CreateBookedReservationRequest(memberId, date, timeId, themeId);
    }
}
