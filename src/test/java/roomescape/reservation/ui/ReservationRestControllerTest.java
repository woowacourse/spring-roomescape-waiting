package roomescape.reservation.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.fixture.ui.LoginApiFixture.adminLoginAndGetCookies;
import static roomescape.fixture.ui.LoginApiFixture.memberLoginAndGetCookies;
import static roomescape.fixture.ui.MemberApiFixture.signUpMembers;
import static roomescape.fixture.ui.MemberApiFixture.signUpRequest1;
import static roomescape.fixture.ui.MemberApiFixture.signUpRequest2;
import static roomescape.fixture.ui.ReservationTimeApiFixture.createReservationTimes;
import static roomescape.fixture.ui.ThemeApiFixture.createThemes;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
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
import roomescape.member.ui.dto.SignUpRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest.ForMember;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;
import roomescape.theme.ui.dto.ThemeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayNameGeneration(ReplaceUnderscores.class)
@DisplayName("회원 예약 관리 API 테스트")
class ReservationRestControllerTest {

    private final LocalDate date = LocalDate.now().plusDays(1);
    private List<ReservationTimeResponse> createReservationTimeResponses;
    private List<ThemeResponse> createThemeResponses;

    @BeforeEach
    void setUp() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        // 관리자 권한으로 예약 시간 추가 (3개)
        createReservationTimeResponses = createReservationTimes(adminCookies, 3);
        // 관리자 권한으로 테마 추가 (2개)
        createThemeResponses = createThemes(adminCookies, 2);
        // 회원 추가 (2명)
        signUpMembers(2);
    }


    @Test
    void 예약을_추가한다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final ForMember reservationParams = bookedReservationRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 과거_시간으로_예약을_추가할_수_없다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final

        ForMember reservationParams = pastBookedReservationRequest();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 로그인_상태가_아니면_예약을_추가할_수_없다() {
        final ForMember reservationParams = bookedReservationRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 예약을_삭제한다() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final ForMember reservationParams = bookedReservationRequest1();

        // member가 예약 추가
        final Integer reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        // admin 권한으로 예약 목록 조회
        RestAssured.given().log().all()
                .cookies(adminCookies)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));

        // member의 예약 삭제
        RestAssured.given().log().all()
                .cookies(memberCookies)
                .when().delete("/reservations/{id}", reservationId)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // admin 권한으로 예약 목록 조회
        RestAssured.given().log().all()
                .cookies(adminCookies)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(0));
    }

    @Test
    void 삭제할_예약이_없는_경우_not_found를_반환한다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 나의_예약_목록을_조회한다() {
        final Map<String, String> adminCookies = adminLoginAndGetCookies();
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final ForMember reservationParams = bookedReservationRequest1();

        final int sizeBeforeCreate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().path("size()");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        final int sizeAfterCreate = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(adminCookies)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("size()");

        Assertions.assertThat(sizeAfterCreate).isEqualTo(sizeBeforeCreate + 1);
    }

    @Test
    void 예약_가능한_시간_목록을_조회한다() {
        final SignUpRequest signUpRequest1 = signUpRequest1();
        final Map<String, String> member1Cookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest1.email(), signUpRequest1.password()));
        final SignUpRequest signUpRequest2 = signUpRequest2();
        final Map<String, String> member2Cookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest2.email(), signUpRequest2.password()));

        final ForMember reservationParams1 = bookedReservationRequest1();
        final ForMember reservationParams2 = bookedReservationRequest2();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(member1Cookies)
                .body(reservationParams1)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(member2Cookies)
                .body(reservationParams2)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        final Long themeId = createThemeResponses.get(0).id();

        final List<AvailableReservationTimeResponse> availableReservationTimeResponses =
                RestAssured.given().log().all()
                        .queryParam("date", date.toString())
                        .queryParam("themeId", themeId)
                        .when().get("/reservations/available-times")
                        .then().log().all()
                        .statusCode(HttpStatus.OK.value())
                        .extract().jsonPath()
                        .getList(".", AvailableReservationTimeResponse.class);

        final long count = availableReservationTimeResponses.stream()
                .filter(availableReservationTimeResponse -> !availableReservationTimeResponse.alreadyBooked())
                .count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void 특정_회원의_예약_목록을_조회한다() {
        final SignUpRequest signUpRequest = signUpRequest1();
        final Map<String, String> memberCookies = memberLoginAndGetCookies(
                new LoginRequest(signUpRequest.email(), signUpRequest.password()));
        final ForMember reservationParams1 = bookedReservationRequest1();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams1)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        final ForMember reservationParams2 = bookedReservationRequest2();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams2)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies(memberCookies)
                .body(reservationParams2)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(2));
    }

    private ForMember bookedReservationRequest1() {
        final Long timeId = createReservationTimeResponses.get(0).id();
        final Long themeId = createThemeResponses.get(0).id();

        return new ForMember(date, timeId, themeId);
    }

    private ForMember bookedReservationRequest2() {
        final Long timeId = createReservationTimeResponses.get(1).id();
        final Long themeId = createThemeResponses.get(0).id();

        return new ForMember(date, timeId, themeId);
    }

    private ForMember pastBookedReservationRequest() {
        final LocalDate date = LocalDate.now().minusDays(5);
        final Long timeId = createReservationTimeResponses.get(0).id();
        final Long themeId = createThemeResponses.get(0).id();

        return new ForMember(date, timeId, themeId);
    }
}
