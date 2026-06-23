package roomescape.reservation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class ReservationControllerTest {

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_DEFAULT_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name, role, store_id)
            VALUES (1, 'brown@email.com', 'password', '브라운', 'USER', NULL),
                   (2, 'manager-gangnam@email.com', 'password', '강남매니저', 'MANAGER', 1);
            """;

    private static final String EMAIL = "brown@email.com";
    private static final String PASSWORD = "password";
    private static final String MANAGER_EMAIL = "manager-gangnam@email.com";
    public static final String INSERT_INTO_RESERVATION_WAIT_RESERVATION_ID_MEMBER_ID = """
            INSERT INTO reservation_wait (reservation_id, member_id, created_at) VALUES (%d, %d, %s)
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Nested
    class 예약_생성 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 정상_요청이면_201과_id를_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 같은_날짜에_다른_시간이면_각각_201을_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams(Map.of("timeId", 2L)))
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201);
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 이미_예약된_시간이면_409를_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams(Map.of("timeId", 1L)))
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(409)
                    .body("errorCode", is("RESERVATION409_001"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void date가_누락되면_400을_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            Map<String, Object> params = reservationParams();
            params.remove("date");

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_001"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 날짜_형식이_잘못되면_400을_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams(Map.of("date", "2026/05/13")))
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_004"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 지난_날짜이면_400을_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams(Map.of(
                            "date", LocalDate.now().minusDays(1).toString()
                    )))
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("RESERVATION400_001"));
        }
    }

    @Nested
    class 예약_삭제 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 성공하면_204를_반환한다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(0));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(0));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 존재하지_않는_예약이면_404를_반환한다() {
            String cookie = authenticate();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/reservations/1")
                    .then().log().all()
                    .statusCode(404)
                    .body("errorCode", is("RESERVATION404_001"));
        }

        @Test
        @Sql(statements = {
                "INSERT INTO member (id, email, password, name) VALUES (1, 'brown@email.com', 'password', '브라운')",
                "INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')",
                "INSERT INTO theme (id, name, description, img_url) VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나옴', 'https://images.example.com/themes/horror-house.jpg')",
                "INSERT INTO store (id, name) VALUES (1, '강남점')",
                "INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id) VALUES (1, 1, DATEADD('DAY', -1, CURRENT_DATE()), 1, 1, 1)"
        })
        void 이미_지난_예약이면_400을_반환한다() {
            String cookie = authenticate();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/reservations/1")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("RESERVATION400_002"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 예약대기가_있는_예약삭제하면_최근예약대기자로_바뀐다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            jdbcTemplate.update(INSERT_INTO_RESERVATION_WAIT_RESERVATION_ID_MEMBER_ID.formatted(1, 2, "'2026-05-27T16:00:00'"));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/reservations/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(0));
        }
    }

    @Nested
    class 예약_변경 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 사용자가_본인의_예약_시간을_변경할_수_있다() {
            String cookie = authenticate();
            createDefaultThemesAsManager();
            createDefaultTimesAsManager();

            Map<String, Object> params = new HashMap<>();
            params.put("date", LocalDate.now().plusDays(1).toString());
            params.put("timeId", 3L);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/api/v1/reservations/1")
                    .then().log().all()
                    .statusCode(200)
                    .body("time.startAt", is("12:00"));
        }
    }

    @Nested
    class 인증_실패 {

        @Test
        void 비로그인_상태로_예약_생성시_401을_반환한다() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 비로그인_상태로_예약_조회시_401을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 잘못된_토큰으로_예약_조회시_401을_반환한다() {
            RestAssured.given().log().all()
                    .auth().oauth2("invalid.jwt.token")
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_003"));
        }
    }

    @Nested
    class 모바일_토큰_인증 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 토큰_로그인_후_Authorization_헤더로_예약을_생성할_수_있다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();
            String accessToken = authenticateAsToken();

            RestAssured.given().log().all()
                    .auth().oauth2(accessToken)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201)
                    .body("memberId", is(1));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 결제_전_대기_예약은_본인_예약_목록에_노출되지_않는다() {
            String cookie = authenticate();
            createDefaultTimesAsManager();
            createDefaultThemesAsManager();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(reservationParams())
                    .when().post("/api/v1/reservations")
                    .then().statusCode(201);

            String accessToken = authenticateAsToken();

            RestAssured.given().log().all()
                    .auth().oauth2(accessToken)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(0));
        }
    }


    @Nested
    class 내_예약과_대기_목록_조회 {

        @Test
        @Sql(statements = {
                INSERT_DEFAULT_STORE_SQL,
                INSERT_DEFAULT_MEMBER_SQL,
                """
                        INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00');
                        """,
                """
                        INSERT INTO theme (id, name, description, img_url)
                        VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나옴',
                                'https://images.example.com/themes/horror-house.jpg');
                        """,
                """
                        INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
                        VALUES (1, 1, '2026-12-01', 1, 1, 1),
                               (2, 1, '2026-12-02', 1, 1, 1),
                               (3, 2, '2026-12-03', 1, 1, 1);
                        """,
                """
                        INSERT INTO reservation_wait (id, reservation_id, member_id, created_at)
                        VALUES (1, 3, 2, '2026-05-27 12:00:01'),
                               (2, 3, 1, '2026-05-27 12:00:05');
                        """
        })
        void 내_확정예약과_내_대기를_순번과_함께_반환한다() {
            String cookie = authenticate();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(2))
                    .body("reservations.memberId", everyItem(is(1)))
                    .body("reservations.id", containsInAnyOrder(1, 2))
                    .body("waitings.size()", is(1))
                    .body("waitings[0].order", is(2))
                    .body("waitings[0].reservationId", is(3))
                    .body("waitings[0].memberId", is(1));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 예약도_대기도_없으면_빈_목록을_반환한다() {
            String cookie = authenticate();

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("reservations.size()", is(0))
                    .body("waitings.size()", is(0));
        }

        @Test
        void 비로그인_상태로_조회하면_401을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }
    }

    private String authenticate() {
        return RestAssured
                .given()
                .param("email", EMAIL)
                .param("password", PASSWORD)
                .when().post("/api/v1/auth/login")
                .then()
                .extract().header("Set-Cookie")
                .split(";")[0];
    }

    private String authenticateAsToken() {
        return RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(EMAIL, PASSWORD))
                .when().post("/api/v1/auth/login/token")
                .then()
                .extract().as(TokenResponse.class)
                .token();
    }

    private void createDefaultTimesAsManager() {
        String managerCookie = authenticateAsManager();
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", managerCookie)
                .body(time)
                .when().post("/api/v1/admin/reservation-times")
                .then().statusCode(201);

        Map<String, String> time2 = new HashMap<>();
        time2.put("startAt", "11:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", managerCookie)
                .body(time2)
                .when().post("/api/v1/admin/reservation-times")
                .then().statusCode(201);

        Map<String, String> time3 = new HashMap<>();
        time3.put("startAt", "12:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", managerCookie)
                .body(time3)
                .when().post("/api/v1/admin/reservation-times")
                .then().statusCode(201);
    }

    private void createDefaultThemesAsManager() {
        String managerCookie = authenticateAsManager();
        Map<String, Object> themeParams = new HashMap<>();
        themeParams.put("name", "이든의 공포 하우스");
        themeParams.put("description", "이든이 귀신으로 나옴");
        themeParams.put("imgUrl", "https://images.example.com/themes/horror-house.jpg");
        RestAssured.given().log().all()
                .header("Cookie", managerCookie)
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/v1/admin/themes")
                .then().statusCode(201);

        Map<String, Object> themeParams2 = new HashMap<>();
        themeParams2.put("name", "정콩이의 방탈출");
        themeParams2.put("description", "니는 못나간다");
        themeParams2.put("imgUrl", "https://images.example.com/themes/jungkong-room.jpg");

        RestAssured.given().log().all()
                .header("Cookie", managerCookie)
                .contentType(ContentType.JSON)
                .body(themeParams2)
                .when().post("/api/v1/admin/themes")
                .then().statusCode(201);
    }

    private String authenticateAsManager() {
        return RestAssured
                .given()
                .param("email", MANAGER_EMAIL)
                .param("password", PASSWORD)
                .when().post("/api/v1/auth/login")
                .then()
                .extract().header("Set-Cookie")
                .split(";")[0];
    }

    private Map<String, Object> reservationParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1L);
        params.put("themeId", 1L);
        params.put("storeId", 1L);
        return params;
    }

    private Map<String, Object> reservationParams(Map<String, Object> overrides) {
        Map<String, Object> params = reservationParams();
        params.putAll(overrides);
        return params;
    }
}
