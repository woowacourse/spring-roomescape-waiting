package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import roomescape.Fixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationResponse;
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
                            defaultMember.getLoginMember()));
            reservation2 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(4), defaultTime, defaultTheme1,
                            defaultMember.getLoginMember()));
            reservation3 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(3), defaultTime, defaultTheme1,
                            defaultMember.getLoginMember()));
            reservation4 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(2), defaultTime, defaultTheme1,
                            defaultMember.getLoginMember()));
            reservation5 = reservationRepository.save(
                    new Reservation(LocalDate.now().minusDays(1), defaultTime, defaultTheme1,
                            defaultMember.getLoginMember()));

            reservation6 = reservationRepository.save(
                    new Reservation(LocalDate.now(), defaultTime, defaultTheme2, defaultMember.getLoginMember()));
            reservation7 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(1), defaultTime, defaultTheme2,
                            defaultMember.getLoginMember()));
            reservation8 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(2), defaultTime, defaultTheme2,
                            defaultMember.getLoginMember()));
            reservation9 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(3), defaultTime, defaultTheme2,
                            defaultMember.getLoginMember()));
            reservation10 = reservationRepository.save(
                    new Reservation(LocalDate.now().plusDays(4), defaultTime, defaultTheme2,
                            defaultMember.getLoginMember()));
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
                    .body("message", is(PAST_TIME_RESERVATION.getMessage()));
        }

        @DisplayName("중복된 예약을 생성할 수 없다.")
        @Test
        void duplicatedReservationTest() {
            RestAssured.given().log().all()
                    .when()
                    .cookie("token", token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "date", reservation6.getDate().toString(),
                            "timeId", reservation6.getReservationTime().getId(),
                            "themeId", reservation6.getTheme().getId()))
                    .post("/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("message", is(DUPLICATE_RESERVATION.getMessage()));
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

        @DisplayName("날짜를 이용해서 검색할 수 있다.")
        @Test
        void searchWithDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .queryParams(Map.of(
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
                    ReservationResponse.from(reservation6),
                    ReservationResponse.from(reservation7)
            );
        }

        @DisplayName("날짜를 입력하지 않고 검색하면 자동으로 오늘의 날짜가 사용된다.")
        @Test
        void searchWithoutDateTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .get("/reservations/search")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .body().as(ReservationResponse[].class);

            assertThat(reservationResponses).containsExactlyInAnyOrder(
                    ReservationResponse.from(reservation6)
            );
        }

        @DisplayName("날짜와 테마를 이용해서 검색할 수 있다.")
        @Test
        void searchWithDateAndThemeTest() {
            ReservationResponse[] reservationResponses = RestAssured.given().log().all()
                    .queryParams(Map.of(
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
                    ReservationResponse.from(reservation5)
            );
        }
    }
}
