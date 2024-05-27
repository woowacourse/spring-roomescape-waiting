package roomescape.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NO_QUERY_PARAMETER;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.Fixture;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.dto.ReservationResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.JwtGenerator;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public class ReservationControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private JwtGenerator JWT_GENERATOR;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    private Theme defaultTheme1 = new Theme("theme1", "description", "thumbnail");
    private Theme defaultTheme2 = new Theme("theme2", "description", "thumbnail");

    private ReservationTime defaultTime = new ReservationTime(LocalTime.of(11, 30));
    private Member defaultMember = Fixture.defaultMember;
    private String token;

    @BeforeEach
    void initData() {
        RestAssured.port = port;
        token = JWT_GENERATOR.generateWith(
                Map.of(
                        "id", defaultMember.getId(),
                        "name", defaultMember.getName(),
                        "role", defaultMember.getRole().getTokenValue()
                )
        );

        defaultTheme1 = themeRepository.save(defaultTheme1);
        defaultTheme2 = themeRepository.save(defaultTheme2);
        defaultTime = reservationTimeRepository.save(defaultTime);
        defaultMember = memberRepository.save(defaultMember);
    }

    @DisplayName("예약이 10개 존재할 때")
    @Nested
    class ExistReservationTest {
        Reservation reservation1;
        Reservation reservation2;
        Reservation reservation3;
        Reservation reservation4;
        Reservation reservation5;
        Reservation reservation6;
        Reservation reservation7;
        Reservation reservation8;
        Reservation reservation9;
        Reservation reservation10;

        @BeforeEach
        void initData() {
            reservation1 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(5), defaultTime, defaultTheme1,
                            defaultMember, ReservationStatus.BOOKED));
            reservation2 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(4), defaultTime, defaultTheme1,
                            defaultMember, ReservationStatus.BOOKED));
            reservation3 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), defaultTime, defaultTheme1,
                            defaultMember, ReservationStatus.BOOKED));
            reservation4 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(2), defaultTime, defaultTheme1,
                            defaultMember, ReservationStatus.BOOKED));
            reservation5 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), defaultTime, defaultTheme1,
                            defaultMember, ReservationStatus.BOOKED));

            reservation6 = reservationRepository.save(
                    new Reservation(LocalDate.now(), defaultTime, defaultTheme1, defaultMember, ReservationStatus.BOOKED));
            reservation7 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1), defaultTime, defaultTheme2,
                            defaultMember, ReservationStatus.BOOKED));
            reservation8 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(2), defaultTime, defaultTheme2,
                            defaultMember, ReservationStatus.BOOKED));
            reservation9 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(3), defaultTime, defaultTheme2,
                            defaultMember, ReservationStatus.BOOKED));
            reservation10 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(4), defaultTime, defaultTheme2,
                            defaultMember, ReservationStatus.BOOKED));
        }

        @DisplayName("존재하는 모든 예약을 조회할 수 있다.")
        @Test
        void getReservationTest() {
            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(10));
        }

        @DisplayName("자신의 모든 예약을 조회할 수 있다.")
        @Test
        void getMembersReservationTest() {
            RestAssured.given().log().all()
                    .cookie("token", token)
                    .when().get("/member/reservation")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(10));
        }

        @DisplayName("예약을 하나 생성할 수 있다.")
        @Test
        void createReservationTest() {
            Map<String, Object> reservationParam = Map.of(
                    "date", LocalDate.now().plusMonths(1).toString(),
                    "timeId", "1",
                    "themeId", "1");

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(11),
                            "member.name", is(defaultMember.getName()),
                            "date", is(reservationParam.get("date")),
                            "time.startAt", is(defaultTime.getStartAt().toString()),
                            "theme.name", is(defaultTheme1.getName()));

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(11));
        }

        @DisplayName("예약 대기 하나를 생성할 수 있다.")
        @Test
        void createWaitingReservationTest() {
            Member testMember = new Member(2L, "test", Role.USER, "test@test.com", "1234");
            memberRepository.save(testMember);
            String newToken = JWT_GENERATOR.generateWith(
                    Map.of(
                            "id", testMember.getId(),
                            "name", testMember.getName(),
                            "role", testMember.getRole().getTokenValue()
                    )
            );
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", newToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation7.getDate().toString(),
                            "timeId", reservation7.getReservationTime().getId(),
                            "themeId", reservation7.getTheme().getId()))
                    .post("/reservations-waiting")
                    .then().log().all()
                    .statusCode(201);
        }

        @DisplayName("지난 시간에 예약을 생성할 수 없다.")
        @Test
        void createPastReservationTest() {
            Map<String, Object> reservationParam = Map.of(
                    "date", LocalDate.now().minusMonths(1).toString(),
                    "timeId", "1",
                    "themeId", "1");

            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(reservationParam)
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(PAST_TIME_RESERVATION.getMessage()));
        }

        @DisplayName("중복된 예약을 생성할 수 없다.")
        @Test
        void duplicatedReservationTest() {
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation7.getDate().toString(),
                            "timeId", reservation7.getReservationTime().getId(),
                            "themeId", reservation7.getTheme().getId()))
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(DUPLICATE_RESERVATION.getMessage()));
        }

        @DisplayName("예약을 하나 삭제할 수 있다.")
        @Test
        void deleteReservationTest() {
            RestAssured.given().log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(9));
        }

        @DisplayName("예약 대기 하나를 삭제할 수 있다.")
        @Test
        void deleteWaitingReservationTest() {
            Member testMember = new Member(2L, "test", Role.USER, "test@test.com", "1234");
            memberRepository.save(testMember);
            String newToken = JWT_GENERATOR.generateWith(
                    Map.of(
                            "id", testMember.getId(),
                            "name", testMember.getName(),
                            "role", testMember.getRole().getTokenValue()
                    )
            );
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", newToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation7.getDate().toString(),
                            "timeId", reservation7.getReservationTime().getId(),
                            "themeId", reservation7.getTheme().getId()))
                    .post("/reservations-waiting")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().cookie("token", newToken)
                    .delete("/reservations-waiting/11")
                    .then().log().all()
                    .statusCode(204);
        }

        @DisplayName("관리자는 예약 대기를 삭제할 수 있다.")
        @Test
        void deleteReservationWaitingByAdminTest() {
            Member testMember = new Member(2L, "test", Role.ADMIN, "test@test.com", "1234");
            memberRepository.save(testMember);
            String newToken = JWT_GENERATOR.generateWith(
                    Map.of(
                            "id", testMember.getId(),
                            "name", testMember.getName(),
                            "role", testMember.getRole().getTokenValue()
                    )
            );
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", newToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation7.getDate().toString(),
                            "timeId", reservation7.getReservationTime().getId(),
                            "themeId", reservation7.getTheme().getId()))
                    .post("/reservations-waiting")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().cookie("token", newToken)
                    .delete("/admin/reservations-waiting/11")
                    .then().log().all()
                    .statusCode(204);
        }

        @DisplayName("관리자는 모든 예약 대기를 조회할 수 있다.")
        @Test
        void getReservationWaitingTest() {
            Member testMember = new Member(2L, "test", Role.ADMIN, "test@test.com", "1234");
            memberRepository.save(testMember);
            String newToken = JWT_GENERATOR.generateWith(
                    Map.of(
                            "id", testMember.getId(),
                            "name", testMember.getName(),
                            "role", testMember.getRole().getTokenValue()
                    )
            );
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", newToken)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation7.getDate().toString(),
                            "timeId", reservation7.getReservationTime().getId(),
                            "themeId", reservation7.getTheme().getId()))
                    .post("/reservations-waiting")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().cookie("token", newToken)
                    .get("/admin/reservations-waiting")
                    .then().log().all()
                    .statusCode(200).body("size()", is(1));
        }

        @DisplayName("날짜를 이용해서 검색할 수 있다.")
        @Test
        void searchWithDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .queryParams(Map.of(
                            "memberId", 1,
                            "themeId", 1,
                            "dateFrom", reservation3.getDate().toString(),
                            "dateTo", reservation7.getDate().toString()
                    ))
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation3),
                    ReservationResponse.from(reservation4),
                    ReservationResponse.from(reservation5),
                    ReservationResponse.from(reservation6)
            );
        }

        @DisplayName("날짜를 입력하지 않고 검색하면 자동으로 오늘의 날짜가 사용된다.")
        @Test
        void searchWithoutDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given()
                    .param("memberId", 1)
                    .param("themeId", 1).log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation6)
            );
        }

        @DisplayName("예약자 아이디를 사용하지 않으면 예외가 발생한다.")
        @Test
        void searchWithoutMemberTest() {
            RestAssured.given()
                    .param("themeId", 1).log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(NO_QUERY_PARAMETER.getMessage()));
        }

        @DisplayName("테마 아이디를 사용하지 않으면 예외가 발생한다.")
        @Test
        void searchWithoutThemeTest() {
            RestAssured.given()
                    .param("memberId", 1).log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(400)
                    .body("detail", is(NO_QUERY_PARAMETER.getMessage()));
        }

        @DisplayName("날짜와 테마를 이용해서 검색할 수 있다.")
        @Test
        void searchWithDateAndThemeTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .queryParams(Map.of(
                            "memberId", 1,
                            "themeId", 1,
                            "dateFrom", reservation3.getDate().toString(),
                            "dateTo", reservation7.getDate().toString()
                    ))
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation3),
                    ReservationResponse.from(reservation4),
                    ReservationResponse.from(reservation5),
                    ReservationResponse.from(reservation6)
            );
        }
    }
}
