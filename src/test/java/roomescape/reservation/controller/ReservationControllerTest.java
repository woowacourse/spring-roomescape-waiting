package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationControllerTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private MemberReservationRepository memberReservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @LocalServerPort
    private int port;


    @Test
    @DisplayName("처음으로 등록하는 예약의 id는 1이다.")
    void firstPost() {
        String accessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        Map<String, String> reservationParams = Map.of(
                "name", "썬",
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "1",
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/reservations/1");
    }

    @Test
    @DisplayName("관리자 권한이 있으면 전체 예약정보를 조회할 수 있다.")
    void readEmptyReservations() {
        // given
        String accessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

        // when
        Reservation reservation1 = reservationRepository.save(new Reservation(LocalDate.now(), reservationTime, theme, member));
        Reservation reservation2 = reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member));
        Reservation reservation3 = reservationRepository.save(new Reservation(LocalDate.now().plusDays(2), reservationTime, theme, member));

        memberReservationRepository.save(new MemberReservation(reservation1, member, ReservationStatus.RESERVED, 0L));
        memberReservationRepository.save(new MemberReservation(reservation2, member, ReservationStatus.RESERVED, 0L));
        memberReservationRepository.save(new MemberReservation(reservation3, member, ReservationStatus.RESERVED, 0L));

        // then
        RestAssured.given().log().all()
                .port(port)
                .header(new Header("Cookie", accessTokenCookie))
                .when().get("/reservations?status=RESERVED")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(3));
    }

    @Test
    @DisplayName("본인의 예약, 예약대기 정보를 조회할 수 있다.")
    void findMemberReservation() {
        // given
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));
        Member anotherMember = memberRepository.save(new Member("name", "another@email.com", "password", Role.MEMBER));
        String accessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(18, 30)));
        Theme theme1 = themeRepository.save(new Theme("테마명1", "설명", "썸네일URL"));
        Theme theme2 = themeRepository.save(new Theme("테마명2", "설명", "썸네일URL"));
        Theme theme3 = themeRepository.save(new Theme("테마명3", "설명", "썸네일URL"));

        LocalDate date = LocalDate.now().plusDays(1);
        reservationService.addReservation(new ReservationRequest(date, reservationTime1.getId(), theme1.getId(), ReservationStatus.RESERVED), member.getId());
        reservationService.addReservation(new ReservationRequest(date, reservationTime2.getId(), theme1.getId(), ReservationStatus.RESERVED), member.getId());
        reservationService.addReservation(new ReservationRequest(date, reservationTime1.getId(), theme2.getId(), ReservationStatus.WAITING), member.getId());
        reservationService.addReservation(new ReservationRequest(date, reservationTime2.getId(), theme2.getId(), ReservationStatus.RESERVED), anotherMember.getId());
        reservationService.addReservation(new ReservationRequest(date, reservationTime1.getId(), theme3.getId(), ReservationStatus.RESERVED), anotherMember.getId());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .when().get("/reservations/my")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(3));
    }

    @Test
    @DisplayName("본인의 예약 정보를 삭제할 수 있다.")
    void canRemoveMyReservation() {
        // given
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));
        String accessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Reservation reservation = reservationRepository.save(new Reservation(LocalDate.now(), reservationTime, theme, member));
        memberReservationRepository.save(new MemberReservation(reservation, member, ReservationStatus.RESERVED, 0L));


        // when & then
        RestAssured.given().log().all()
                .port(port)
                .header("Cookie", accessTokenCookie)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("본인의 예약이 아니면 예약 정보를 삭제할 수 없으며 403 Forbidden 을 Response 받는다.")
    void canRemoveAnotherReservation() {
        // given
        Member member = memberRepository.save(new Member("name", "member1@email.com", "password", Role.MEMBER));
        String accessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        Member anotherMember = memberRepository.save(new Member("name1", "member2@email.com", "password", Role.MEMBER));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        Reservation reservation = reservationRepository.save(new Reservation(LocalDate.now(), reservationTime, theme, anotherMember));
        memberReservationRepository.save(new MemberReservation(reservation, anotherMember, ReservationStatus.RESERVED, 0L));

        // when & then
        RestAssured.given().log().all()
                .port(port)
                .header("Cookie", accessTokenCookie)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("본인의 예약이 아니더라도 관리자 권한이 있으면 예약 정보를 삭제할 수 있다.")
    void readReservationsSizeAfterPostAndDelete() {
        // given
        Member member = memberRepository.save(new Member("name", "admin@admin.com", "password", Role.ADMIN));
        String accessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member anotherMember = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

        Reservation reservation = reservationRepository.save(new Reservation(LocalDate.now(), reservationTime, theme, anotherMember));
        memberReservationRepository.save(new MemberReservation(reservation, anotherMember, ReservationStatus.RESERVED, 0L));

        // when & then
        RestAssured.given().log().all()
                .port(port)
                .header("Cookie", accessTokenCookie)
                .when().delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("특정 날짜의 특정 테마 예약 현황을 조회한다.")
    void readReservationByDateAndThemeId() {
        // given
        LocalDate today = LocalDate.now();
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 00)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        ReservationTime reservationTime3 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(18, 30)));
        Theme theme = themeRepository.save(new Theme("테마명1", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

        reservationRepository.save(new Reservation(today.plusDays(1), reservationTime1, theme, member));
        reservationRepository.save(new Reservation(today.plusDays(1), reservationTime2, theme, member));
        reservationRepository.save(new Reservation(today.plusDays(1), reservationTime3, theme, member));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .when().get("/reservations/themes/1/times?date=" + today.plusDays(1))
                .then().log().all()
                .statusCode(200)
                .body("data.reservationTimes.size()", is(3));
    }

    @ParameterizedTest
    @MethodSource("requestValidateSource")
    @DisplayName("예약 생성 시, 요청 값에 공백 또는 null이 포함되어 있으면 400 에러를 발생한다.")
    void validateBlankRequest(Map<String, String> invalidRequestBody) {
        String accessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
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
        String accessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        Map<String, String> invalidTypeRequestBody = Map.of(
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "한글"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .body(invalidTypeRequestBody)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜/시간/테마로 예약 생성 요청 시, 409 에러를 발생한다.")
    void validateDateTimeTheme() {
        // given
        String accessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        Map<String, String> reservationParams = Map.of(
                "name", "썬",
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "1",
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/reservations/1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("다른 회원의 예약 대기 정보를 삭제 요청 시, 403 Forbidden 이 발생한다.")
    void failToRemoveAnotherMemberWaiting() {
        // given
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));
        Member anotherMember = memberRepository.save(new Member("name", "another@email.com", "password", Role.MEMBER));
        String accessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());
        String anotherAccessTokenCookie = getAccessTokenCookieByLogin(anotherMember.getEmail(), anotherMember.getPassword());

        reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        Map<String, String> reservationParams = Map.of(
                "name", "썬",
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "1",
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", accessTokenCookie)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/reservations/1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", anotherAccessTokenCookie)
                .body(reservationParams)
                .when().delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("관리자는 모든 회원의 예약 대기 정보를 삭제할 수 있다.")
    void canRemoveAnotherMemberWaitingByAdminRole() {
        // given
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");
        Member member = memberRepository.save(new Member("name", "another@email.com", "password", Role.MEMBER));
        String memberAccessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));

        Map<String, String> reservationParams = Map.of(
                "name", "썬",
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", "1",
                "themeId", "1",
                "status", ReservationStatus.RESERVED.name()
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", memberAccessTokenCookie)
                .body(reservationParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("data.id", is(1))
                .header("Location", "/reservations/1");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", adminAccessTokenCookie)
                .body(reservationParams)
                .when().delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("관리자가 회원의 예약 대기를 승인하면 순서가 한칸씩 앞당겨지고, 승인된 예약은 대기 상태에서 예약 상태로 변경된다.")
    void canApproveMemberWaitingByAdminRole() {
        // given
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");
        Member member1 = memberRepository.save(new Member("name", "another1@email.com", "password", Role.MEMBER));
        Member member2 = memberRepository.save(new Member("name", "another2@email.com", "password", Role.MEMBER));
        Member member3 = memberRepository.save(new Member("name", "another3@email.com", "password", Role.MEMBER));

        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        LocalDate tomorrow = LocalDate.now().plusDays(1L);

        reservationService.addReservation(new ReservationRequest(tomorrow, time.getId(), theme.getId(), ReservationStatus.RESERVED), member1.getId());
        reservationService.addReservation(new ReservationRequest(tomorrow, time.getId(), theme.getId(), ReservationStatus.WAITING), member2.getId());
        reservationService.addReservation(new ReservationRequest(tomorrow, time.getId(), theme.getId(), ReservationStatus.WAITING), member3.getId());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", adminAccessTokenCookie)
                .when().patch("/reservations/waitings/2")
                .then().log().all()
                .statusCode(200);

        Optional<MemberReservation> optionalMember2Reservation = memberReservationRepository.findByMemberAndReservationTimeAndDateAndTheme(member2, time, tomorrow, theme);
        Optional<MemberReservation> optionalMember3Reservation = memberReservationRepository.findByMemberAndReservationTimeAndDateAndTheme(member3, time, tomorrow, theme);

        Assertions.assertThat(optionalMember2Reservation).isNotEmpty();
        Assertions.assertThat(optionalMember3Reservation).isNotEmpty();
        Assertions.assertThat(optionalMember2Reservation.get().isReserved()).isTrue();

        Assertions.assertThat(optionalMember2Reservation.get().getOrder()).isEqualTo(0L);
        Assertions.assertThat(optionalMember3Reservation.get().getOrder()).isEqualTo(1L);
    }

    @Test
    @DisplayName("관리자가 아니라면 예약대기를 승인해줄 수 없으며, 403 Forbidden 이 발생한다.")
    void cannotApproveMemberWaitingByMemberRole() {
        // given
        Member member = memberRepository.save(new Member("name", "member1@email.com", "password", Role.MEMBER));
        String memberAccessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", memberAccessTokenCookie)
                .when().patch("/reservations/waitings/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("관리자는 전체 예약 대기 정보 조회 시, 첫 번째 순서로 대기중인 예약 정보를 조회할 수 있다.")
    void findFirstOrderWaitingReservationsWithAdminRole() {
        // given
        String adminAccessTokenCookie = getAdminAccessTokenCookieByLogin("admin@admin.com", "12341234");

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1L);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(tomorrow.toLocalTime()));
        Theme theme1 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Theme theme2 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member1 = memberRepository.save(new Member("name1", "email1@email.com", "password", Role.MEMBER));
        Member member2 = memberRepository.save(new Member("name2", "email2@email.com", "password", Role.MEMBER));
        Member member3 = memberRepository.save(new Member("name3", "email3@email.com", "password", Role.MEMBER));

        reservationService.addReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId(), ReservationStatus.RESERVED), member1.getId());
        ReservationResponse firstWaitingOrder1 = reservationService.addReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId(), ReservationStatus.WAITING), member2.getId());
        reservationService.addReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme2.getId(), ReservationStatus.RESERVED), member2.getId());
        ReservationResponse firstWaitingOrder2 = reservationService.addReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme2.getId(), ReservationStatus.WAITING), member3.getId());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", adminAccessTokenCookie)
                .when().get("/reservations/waitings")
                .then().log().all()
                .statusCode(200)
                .body("data.reservations.size()", is(2));

        List<ReservationResponse> firstOrderWaitingReservations = reservationService.findFirstOrderWaitingReservations().reservations();
        Assertions.assertThat(firstOrderWaitingReservations.get(0).id()).isEqualTo(firstWaitingOrder1.id());
        Assertions.assertThat(firstOrderWaitingReservations.get(1).id()).isEqualTo(firstWaitingOrder2.id());
    }

    @Test
    @DisplayName("관리자가 아니면 전체 예약 대기 정보를 조회할 수 없고, 403 Forbidden 이 발생한다.")
    void cannotFindFirstOrderWaitingReservationsWithMemberRole() {
        // given
        Member member = memberRepository.save(new Member("name", "member1@email.com", "password", Role.MEMBER));
        String memberAccessTokenCookie = getAccessTokenCookieByLogin(member.getEmail(), member.getPassword());

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .header("Cookie", memberAccessTokenCookie)
                .when().get("/reservations/waitings")
                .then().log().all()
                .statusCode(403);
    }

    private String getAdminAccessTokenCookieByLogin(final String email, final String password) {
        memberRepository.save(new Member("이름", email, password, Role.ADMIN));

        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        String accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login")
                .then().log().all().extract().cookie("accessToken");

        return "accessToken=" + accessToken;
    }

    private String getAccessTokenCookieByLogin(final String email, final String password) {
        Map<String, String> loginParams = Map.of(
                "email", email,
                "password", password
        );

        String accessToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .port(port)
                .body(loginParams)
                .when().post("/login")
                .then().log().all().extract().cookie("accessToken");

        return "accessToken=" + accessToken;
    }
}
