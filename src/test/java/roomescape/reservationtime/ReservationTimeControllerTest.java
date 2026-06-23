package roomescape.reservationtime;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class ReservationTimeControllerTest {
    private static final String AVAILABLE_TIME_TEST_DATE = "2099-05-05";

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

    @Nested
    class 예약시간_조회 {

        @Test
        void 성공하면_200을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservation-times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }
    }

    @Nested
    class 예약가능시간_조회 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 성공하면_200을_반환한다() {
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
                    .when().get("/api/v1/reservation-times/availability?date=" + AVAILABLE_TIME_TEST_DATE
                            + "&themeId=1&storeId=1")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3))
                    .body("find { it.id == 1 }.time", is("10:00"))
                    .body("find { it.id == 1 }.available", is(false))
                    .body("findAll { it.available == true }.size()", is(2));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 같은_날짜와_시간이어도_테마가_다르면_각각_예약_가능한_슬롯으로_취급된다() {
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
                    .body(reservationParams(Map.of("themeId", 2L)))
                    .when().post("/api/v1/reservations")
                    .then().log().all()
                    .statusCode(201);
        }

        @Test
        void date가_누락되면_400을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservation-times/availability?themeId=1")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_003"));
        }

        @Test
        void themeId가_누락되면_400을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservation-times/availability?date=2026-05-15")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_003"));
        }

        @Test
        void date_형식이_잘못되면_400을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservation-times/availability?date=2026/05/15&themeId=1")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_005"));
        }

        @Test
        void themeId_형식이_잘못되면_400을_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/reservation-times/availability?date=2026-05-15&themeId=abc")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_005"));
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
        params.put("date", AVAILABLE_TIME_TEST_DATE);
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
