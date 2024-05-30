package roomescape.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.support.dto.TokenCookieDto;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

public class ReservationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("회원권한이 있으면 예약을 등록할 수 있다.")
    void reservationHasRole() {
        TokenCookieDto tokenCookieDto = cookieProvider.saveMemberAndGetJwtTokenCookies("member@email.com", "12341234", port);

        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        Map<String, String> reservationParams = Map.of(
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString(),
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/reservations/1");
    }

    @Test
    @DisplayName("예약이 존재하지 않으면 예약대기 등록을 할 수 없다.")
    void cannotReservationWaitingBecauseReservationNotExist() {
        TokenCookieDto tokenCookieDto = cookieProvider.saveMemberAndGetJwtTokenCookies("email@email.com", "password", port);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        Map<String, String> reservationParams = Map.of(
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .body(reservationParams)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("관리자 권한이 있으면 전체 예약정보를 조회할 수 있다.")
    void readEmptyReservations() {
        // given
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);

        Member member = memberFixture.createMember();
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail1 = reservationDetailFixture.createReservationDetail(LocalDate.now(), time, theme);
        ReservationDetail reservationDetail2 = reservationDetailFixture.createReservationDetail(LocalDate.now(), time, theme);

        reservationFixture.createReservation(reservationDetail1, member);
        reservationFixture.createReservation(reservationDetail2, member);

        // when & then
        RestAssured.given().log().all()
                .header(new Header("Cookie", adminTokenCookieDto.accessTokenCookie()))
                .when().get("/admin/reservations?status=RESERVED")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(2));
    }

    @Test
    @DisplayName("본인의 예약, 예약대기 정보를 조회할 수 있다.")
    void findMemberReservation() {
        // given
        Member member = memberFixture.createMember();
        TokenCookieDto memberTokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        Member anotherMember = memberFixture.createMember("another@email.com", "12341234");
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        reservationFixture.createReservation(reservationDetail, member);
        reservationFixture.createWaiting(reservationDetail, member);
        reservationFixture.createReservation(reservationDetail, anotherMember);
        reservationFixture.createWaiting(reservationDetail, anotherMember);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", memberTokenCookieDto.accessTokenCookie())
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(2));
    }

    @Test
    @DisplayName("본인의 예약 정보를 삭제할 수 있다.")
    void canRemoveMyReservation() {
        // given
        Member member = memberFixture.createMember();
        TokenCookieDto tokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        MemberReservation reservation = reservationFixture.createReservation(reservationDetail, member);

        // when & then
        RestAssured.given().log().all()
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예약 정보를 삭제할 수 없으며 403 Forbidden 을 Response 받는다.")
    void canRemoveAnotherReservation() {
        // given
        Member member = memberFixture.createMember();
        TokenCookieDto memberTokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        Member anotherMember = memberFixture.createMember("another@email.com", "12341234");
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        MemberReservation anotherMemberReservation = reservationFixture.createReservation(reservationDetail, anotherMember);

        // when & then
        RestAssured.given().log().all()
                .header("Cookie", memberTokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/" + anotherMemberReservation.getId())
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("본인의 예약이 아니더라도 관리자 권한이 있으면 예약 정보를 삭제할 수 있다.")
    void readReservationsSizeAfterPostAndDelete() {
        // given
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@email.com", "password", port);

        Member member = memberFixture.createMember();
        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();

        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        MemberReservation reservation = reservationFixture.createReservation(reservationDetail, member);

        // when & then
        RestAssured.given().log().all()
                .header("Cookie", adminTokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("특정 날짜의 특정 테마 예약 현황을 조회한다.")
    void readReservationByDateAndThemeId() {
        // given
        Member member = memberFixture.createMember();

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail1 = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);
        ReservationDetail reservationDetail2 = reservationDetailFixture.createReservationDetail(yesterday, time, theme);

        reservationFixture.createReservation(reservationDetail1, member);
        reservationFixture.createReservation(reservationDetail2, member);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/themes/" + theme.getId() + "/times?date=" + tomorrow)
                .then().log().all()
                .statusCode(200)
                .body("data.reservationTimes.size()", is(1));
    }

    @ParameterizedTest
    @MethodSource("requestValidateSource")
    @DisplayName("예약 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> invalidRequestBody) {
        TokenCookieDto tokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("member@email.com", "12341234", port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .body(invalidRequestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    private static Stream<Map<String, String>> requestValidateSource() {
        return Stream.of(
                Map.of("timeId", "1",
                        "themeId", "1"),

                Map.of("date", LocalDate.now().plusDays(1L).toString(),
                        "themeId", "1"),

                Map.of("date", LocalDate.now().plusDays(1L).toString(),
                        "timeId", "1"),

                Map.of("date", " ",
                        "timeId", "1",
                        "themeId", "1"),

                Map.of("date", LocalDate.now().plusDays(1L).toString(),
                        "timeId", " ",
                        "themeId", "1"),

                Map.of("date", LocalDate.now().plusDays(1L).toString(),
                        "timeId", "1",
                        "themeId", " ")
        );
    }

    @Test
    @DisplayName("예약 생성 시, 정수 요청 데이터에 문자가 입력되어오면 400 에러를 발생한다.")
    void validateRequestDataFormat() {
        TokenCookieDto tokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);

        Map<String, String> invalidTypeRequestBody = Map.of(
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "한글"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .body(invalidTypeRequestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜/시간/테마로 예약 생성 요청 시, 409 에러를 발생한다.")
    void validateDateTimeThemeDuplication() {
        // given
        Member member = memberFixture.createMember();
        Member anotherMember = memberFixture.createMember("another@email.com", "12341234");
        TokenCookieDto myTokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        reservationFixture.createReservation(reservationDetail, anotherMember);

        // when & then
        Map<String, String> reservationParams = Map.of(
                "date", tomorrow.toString(),
                "timeId", time.getId().toString(),
                "themeId", theme.getId().toString(),
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", myTokenCookieDto.accessTokenCookie())
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("다른 회원의 예약 대기 정보를 삭제 요청 시, 403 Forbidden 이 발생한다.")
    void failToRemoveAnotherMemberWaiting() {
        // given
        Member member = memberFixture.createMember();
        Member anotherMember = memberFixture.createMember("another@email.com", "12341234");
        TokenCookieDto myTokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        MemberReservation anotherMemberWaitingReservation = reservationFixture.createWaiting(reservationDetail, anotherMember);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", myTokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/waitings/" + anotherMemberWaitingReservation.getId())
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("자신의 예약 대기 정보를 삭제할 수 있다.")
    void removeAnotherMemberWaiting() {
        // given
        Member member = memberFixture.createMember();
        TokenCookieDto myTokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        MemberReservation myWaitingReservation = reservationFixture.createWaiting(reservationDetail, member);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", myTokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/waitings/" + myWaitingReservation.getId())
                .then().log().all()
                .statusCode(204);

        int reservationSize = reservationFixture.findAll().size();
        Assertions.assertThat(reservationSize).isEqualTo(0);
    }

    @Test
    @DisplayName("관리자는 모든 회원의 예약 대기 정보를 삭제할 수 있다.")
    void canRemoveAnotherMemberWaitingByAdminRole() {
        // given
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);
        Member waitingMember = memberFixture.createMember();

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        MemberReservation waitingReservation = reservationFixture.createWaiting(reservationDetail, waitingMember);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", adminTokenCookieDto.accessTokenCookie())
                .when().delete("/reservations/waitings/" + waitingReservation.getId())
                .then().log().all()
                .statusCode(204);

        int waitingReservationSize = reservationFixture.findAll().size();
        Assertions.assertThat(waitingReservationSize).isEqualTo(0);
    }

    @Test
    @DisplayName("관리자가 회원의 예약 대기를 승인하면, 승인된 예약은 대기 상태에서 예약 상태로 변경된다.")
    void canApproveMemberWaitingByAdminRole() {
        // given
        TokenCookieDto adminTokenCookieDto = cookieProvider.saveAdminAndGetTokenCookies("admin@admin.com", "12341234", port);

        Member waitingMember = memberFixture.createMember();

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime(LocalTime.of(17, 30));
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        MemberReservation waiting = reservationFixture.createWaiting(reservationDetail, waitingMember);
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", adminTokenCookieDto.accessTokenCookie())
                .when().patch("/admin/reservations/waitings/" + waiting.getId())
                .then().log().all()
                .statusCode(200);

        MemberReservation memberReservation = reservationFixture.findById(waiting.getId());
        Assertions.assertThat(memberReservation.isReservedStatus()).isTrue();
    }

    @Test
    @DisplayName("관리자가 아니라면 예약대기를 승인해줄 수 없으며, 403 Forbidden 이 발생한다.")
    void cannotApproveMemberWaitingByMemberRole() {
        // given
        TokenCookieDto tokenCookieDto = cookieProvider.saveMemberAndGetJwtTokenCookies("member1@email.com", "password", port);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .when().patch("/admin/reservations/waitings/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("관리자는 첫 번째 순서로 대기중인 예약 정보 전체를 조회할 수 있다.")
    void findFirstOrderWaitingReservationsWithAdminRole() {
        // given
        Member waitingMember1 = memberFixture.createMember("waiting1@email.com");
        Member waitingMember2 = memberFixture.createMember("waiting2@email.com");

        LocalDate tomorrow = LocalDate.now().plusDays(1L);
        ReservationTime time = reservationTimeFixture.createTime();
        Theme theme = themeFixture.createTheme();
        ReservationDetail reservationDetail = reservationDetailFixture.createReservationDetail(tomorrow, time, theme);

        reservationFixture.createWaiting(reservationDetail, waitingMember1);
        reservationFixture.createWaiting(reservationDetail, waitingMember2);

        // when & then
        Member admin = memberFixture.createAdmin();
        TokenCookieDto adminTokenCookieDto = cookieProvider.loginAndGetTokenCookies(admin.getEmail(), admin.getPassword(), port);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", adminTokenCookieDto.accessTokenCookie())
                .when().get("/admin/reservations/waitings")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(2));
    }

    @Test
    @DisplayName("관리자가 아니면 전체 예약 대기 정보를 조회할 수 없고, 403 Forbidden 이 발생한다.")
    void cannotFindFirstOrderWaitingReservationsWithMemberRole() {
        // given
        Member member = memberFixture.createMember();
        TokenCookieDto tokenCookieDto = cookieProvider.loginAndGetTokenCookies(member.getEmail(), member.getPassword(), port);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", tokenCookieDto.accessTokenCookie())
                .when().get("/admin/reservations/waitings")
                .then().log().all()
                .statusCode(403);
    }
}
