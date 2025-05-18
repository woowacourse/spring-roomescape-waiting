package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.common.Constant.예약날짜_내일;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.BaseTest;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.member.role.Role;
import roomescape.member.service.AuthService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeJpaRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeJpaRepository;

@Sql("/test-data.sql")
public class ReservationTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeJpaRepository themeJpaRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    EntityManager em;

    private Theme theme;

    private Member member;

    private Map<String, Object> reservation;

    private String token;

    @BeforeEach
    void setUp() {

        RestAssured.port = port;

        theme = themeJpaRepository.save(new Theme("테마1", "설명1", "썸네일1"));

        member = memberRepository.save(
                new Member(new Name("매트"), new Email("matt@kakao.com"), new Password("1234"), Role.ADMIN));

        reservationTimeJpaRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        token = authService.tokenLogin(new TokenLoginCreateRequest("matt@kakao.com", "1234")).tokenResponse();

        reservation = new HashMap<>();
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);
    }

    @Test
    void 방탈출_예약을_생성_조회_삭제한다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_시간을_조회_삭제한다() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 관리자_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_목록을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/admin/reservations/999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/times/3")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 방탈출_예약_목록을_조회한다() {

        ReservationTime reservationTime = reservationTimeJpaRepository.save(
                ReservationTime.create(LocalTime.of(10, 0)));
        reservationRepository.save(
                Reservation.create(예약날짜_내일.getDate(), reservationTime, theme, member, ReservationStatus.RESERVATION));

        List<ReservationResponse> response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    void 방탈출_예약_목록을_생성_조회_삭제한다() {

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 날짜가_null이면_예외를_응답한다() {
        Map<String, Object> reservationFail = new HashMap<>();
        reservationFail.put("name", "브라운");
        reservationFail.put("timeId", 1);
        reservationFail.put("themeId", 1);

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationFail)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 시간이_null이면_예외를_응답한다() {
        Map<String, String> reservationFail = new HashMap<>();
        reservationFail.put("startAt", "");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationFail)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 멤버의_예약목록을_가져온다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
