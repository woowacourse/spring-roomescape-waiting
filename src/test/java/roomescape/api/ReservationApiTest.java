package roomescape.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationApiTest {

    private static final Map<String, Object> RESERVATION_BODY = new HashMap<>();
    private static final Map<String, String> TIME_BODY = new HashMap<>();
    private static final Map<String, String> THEME_BODY = new HashMap<>();
    private static final Map<String, String> MEMBER_BODY = new HashMap<>();
    private static final Map<String, Object> AUTH_BODY = new HashMap<>();
    private static final Map<String, Object> SCHEDULE_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public ReservationApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeAll
    static void initParams() {
        SCHEDULE_BODY.put("date", LocalDate.of(2026, 12, 1));
        SCHEDULE_BODY.put("reservationTimeId", 1L);
        SCHEDULE_BODY.put("themeId", 1L);

        RESERVATION_BODY.put("date", LocalDate.of(2026, 12, 1));
        RESERVATION_BODY.put("timeId", 1L);
        RESERVATION_BODY.put("themeId", 1L);

        TIME_BODY.put("startAt", "10:00");

        THEME_BODY.put("name", "theme");
        THEME_BODY.put("description", "dest");
        THEME_BODY.put("thumbnail", "thumbnail");

        MEMBER_BODY.put("name", "브라운");
        MEMBER_BODY.put("email", "asd@email.com");
        MEMBER_BODY.put("password", "pass");

        AUTH_BODY.put("email", "asd@email.com");
        AUTH_BODY.put("password", "pass");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM schedule");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE schedule ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
    }

    @Nested
    @DisplayName("예약 생성")
    class Post {

        @DisplayName("reservation POST 요청 테스트")
        @ParameterizedTest
        @MethodSource
        void post(final Map<String, Object> body, final HttpStatus expectedStatusCode) {
            // given
            givenCreateMember();
            final Cookie cookie = givenAuthCookie();
            givenCreateReservationTime();
            givenCreateTheme();
            givenCreateSchedule();

            // when & then
            RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .cookie(cookie)
                    .body(body)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(expectedStatusCode.value());
        }

        static Stream<Arguments> post() {
            return Stream.of(
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L,
                            "themeId", 1L
                    ), HttpStatus.CREATED),

                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "timeId", 1L,
                            "themeId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "themeId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L
                    ), HttpStatus.BAD_REQUEST),

                    Arguments.of(Map.of(
                            "date", "",
                            "timeId", 1L,
                            "themeId", 1L
                    ), HttpStatus.BAD_REQUEST),

                    Arguments.of(Map.of(), HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("존재하지 않는 시간을 선택하면, 400을 응답한다.")
        @Test
        void post2() {
            // given
            givenCreateMember();
            final Cookie cookie = givenAuthCookie();
            givenCreateTheme();

            Map<String, Object> reservation = new HashMap<>(RESERVATION_BODY);
            reservation.put("timeId", 2L);

            // when & then
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .cookie(cookie)
                    .body(reservation)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @DisplayName("존재하지 않는 테마를 선택하면, 400을 응답한다.")
        @Test
        void post3() {
            // given
            givenCreateMember();
            final Cookie cookie = givenAuthCookie();
            givenCreateReservationTime();

            Map<String, Object> reservation = new HashMap<>(RESERVATION_BODY);
            reservation.put("themeId", 2L);

            // when & then
            RestAssured.given().port(port).log().all()
                    .contentType(ContentType.JSON)
                    .cookie(cookie)
                    .body(reservation)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

    }

    @Nested
    @DisplayName("예약 조회")
    class Get {

        @DisplayName("존재하는 모든 예약과 200 OK를 응답")
        @Test
        void get1() {
            // given
            givenCreateMember();
            final Cookie cookie = givenAuthCookie();
            givenCreateReservationTime();
            givenCreateTheme();
            givenCreateSchedule();
            givenCreateReservation(cookie);

            // when & then
            RestAssured.given().port(port).log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("예약이 존재하지 않는다면 200 OK와 빈 컬렉션 응답")
        @Test
        void get2() {
            // given & when & then
            RestAssured.given().port(port).log().all()
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

    }

    @Nested
    @DisplayName("예약 삭제")
    class Delete {

        @DisplayName("주어진 아이디에 해당하는 예약이 있다면 200 OK 응답")
        @Test
        void remove1() {
            // given
            givenCreateMember();
            final Cookie cookie = givenAuthCookie();
            givenCreateReservationTime();
            givenCreateTheme();
            givenCreateSchedule();
            givenCreateReservation(cookie);

            // when & then
            RestAssured.given().port(port).log().all()
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @DisplayName("주어진 아이디에 해당하는 예약이 없다면 404로 응답한다.")
        @Test
        void remove2() {
            // given & when & then
            RestAssured.given().port(port).log().all()
                    .when().delete("/reservations/1000")
                    .then().log().all()
                    .statusCode(404);
        }
    }

    private void givenCreateReservationTime() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(TIME_BODY)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateReservation(final Cookie cookie) {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(RESERVATION_BODY)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateTheme() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(THEME_BODY)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateSchedule() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(SCHEDULE_BODY)
                .when().post("/schedules")
                .then().log().all()
                .statusCode(201);
    }

    private void givenCreateMember() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(MEMBER_BODY)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    private Cookie givenAuthCookie() {
        return RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(AUTH_BODY)
                .when().post("/login")
                .then()
                .extract().detailedCookie("token");
    }
}
