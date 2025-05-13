package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdminReservationApiTest {

    private static final Map<String, String> RESERVATION_BODY = new HashMap<>();
    private static final Map<String, String> TIME_BODY = new HashMap<>();
    private static final Map<String, String> THEME_BODY = new HashMap<>();
    private static final Map<String, Object> AUTH_BODY = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;
    private final int port;

    public AdminReservationApiTest(
            @LocalServerPort final int port,
            @Autowired final JdbcTemplate jdbcTemplate
    ) {
        this.port = port;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeAll
    static void initParams() {
        RESERVATION_BODY.put("date", "2026-08-05");
        RESERVATION_BODY.put("timeId", "1");
        RESERVATION_BODY.put("themeId", "1");
        RESERVATION_BODY.put("memberId", "1");

        TIME_BODY.put("startAt", "10:00");

        THEME_BODY.put("name", "theme");
        THEME_BODY.put("description", "dest");
        THEME_BODY.put("thumbnail", "thumbnail");

        AUTH_BODY.put("email", "admin@email.com");
        AUTH_BODY.put("password", "password");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO member (email, password, name, role) VALUES (?, ?, ?, ?)",
                "admin@email.com", "password", "name", "ADMIN");
    }

    @Nested
    @DisplayName("예약 생성")
    class Post {

        @DisplayName("reservation POST 요청 테스트")
        @ParameterizedTest
        @MethodSource
        void post(final Map<String, Object> body, final HttpStatus expectedStatusCode) {
            // given
            final Cookie cookie = givenAdminAuthCookie();
            givenCreateReservationTime();
            givenCreateTheme();

            // when & then
            RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .cookie(cookie)
                    .body(body)
                    .when().post("/admin/reservations")
                    .then().log().all()
                    .statusCode(expectedStatusCode.value());
        }

        static Stream<Arguments> post() {
            return Stream.of(
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L,
                            "themeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.CREATED),

                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "timeId", 1L,
                            "themeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "themeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.BAD_REQUEST),
                    Arguments.of(Map.of(
                            "date", "2026-12-01",
                            "timeId", 1L,
                            "themeId", 1L
                    ), HttpStatus.BAD_REQUEST),

                    Arguments.of(Map.of(
                            "date", "",
                            "timeId", 1L,
                            "themeId", 1L,
                            "memberId", 1L
                    ), HttpStatus.BAD_REQUEST),

                    Arguments.of(Map.of(), HttpStatus.BAD_REQUEST)
            );
        }
    }

    @Nested
    @DisplayName("테마, 멤버, 날짜 범위 기반 필터링 Reservation 조회")
    class ReadAllByMemberAndThemeAndDateRange {

        @DisplayName("reservation 필터 기반 모두 조회")
        @ParameterizedTest
        @CsvSource(value = {
                "1:1:2025-05-07:2025-05-07",
                "1:2:2025-05-07:2025-05-07",
                "1:2:2025-05-08:2025-05-08"
        }, delimiter = ':')
        void readAllByMemberAndThemeAndDateRange(final Long memberId, final Long themeId, final String from,
                                                 final String to) {
            // given
            final Cookie cookie = givenAdminAuthCookie();
            givenCreateReservationTime();
            jdbcTemplate.update("""
                        INSERT INTO theme (name, description, thumbnail) VALUES
                        ('theme1', 'des1', 'thumb1'),
                        ('theme2', 'des2', 'thumb2');
                    """);
            jdbcTemplate.update("""
                        INSERT INTO reservation (date, member_id, time_id, theme_id) VALUES
                        ('2025-05-07', 1, 1, 1),
                        ('2025-05-07', 1, 1, 2),
                        ('2025-05-08', 1, 1, 2);
                    """);

            // when & then
            RestAssured
                .given().log().all()
                    .port(port)
                    .contentType(ContentType.JSON)
                    .cookie(cookie)
                    .queryParam("memberId", memberId)
                    .queryParam("themeId", themeId)
                    .queryParam("from", from)
                    .queryParam("to", to)
                .when()
                    .get("/admin/reservations")
                .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1));
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

    private void givenCreateTheme() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(THEME_BODY)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    private Cookie givenAdminAuthCookie() {
        return RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "admin@email.com",
                        "password", "password"
                ))
                .when().post("/login")
                .then()
                .extract().detailedCookie("token");
    }

}
