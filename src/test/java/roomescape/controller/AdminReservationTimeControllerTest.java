package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
public class AdminReservationTimeControllerTest {

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_DEFAULT_MEMBER_SQL = """
            INSERT INTO member (id, email, password, name, role, store_id)
            VALUES (1, 'manager-gangnam@email.com', 'password', '강남매니저', 'MANAGER', 1);
            """;

    private static final String EMAIL = "manager-gangnam@email.com";
    private static final String PASSWORD = "password";

    @Nested
    class 예약시간_생성 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 성공하면_201을_반환한다() {
            String cookie = authenticate();

            RestAssured.given().contentType(ContentType.JSON)
                    .header("Cookie", cookie)
                    .body(timeParams())
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void startAt이_누락되면_400을_반환한다() {
            String cookie = authenticate();

            RestAssured.given().contentType(ContentType.JSON)
                    .header("Cookie", cookie)
                    .body("{}")
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_001"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 시작시간_형식이_잘못되면_400을_반환한다() {
            String cookie = authenticate();

            Map<String, Object> params = new HashMap<>();
            params.put("startAt", "10:00:00");

            RestAssured.given().contentType(ContentType.JSON)
                    .header("Cookie", cookie)
                    .body(params)
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(400)
                    .body("errorCode", is("COMMON400_004"));
        }
    }

    @Nested
    class 예약시간_삭제 {

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 성공하면_204를_반환한다() {
            String cookie = authenticate();

            RestAssured.given().contentType(ContentType.JSON)
                    .header("Cookie", cookie)
                    .body(timeParams())
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            RestAssured.given().log().all()
                    .when().get("/api/v1/reservations/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/times/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/api/v1/reservations/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 존재하지_않는_예약시간이면_404를_반환한다() {
            String cookie = authenticate();
            createDefaultTimes(cookie);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/times/4")
                    .then().log().all()
                    .statusCode(404)
                    .body("errorCode", is("RESERVATION_TIME404_001"));
        }

        @Test
        @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
        void 예약이_존재하면_409를_반환한다() {
            String cookie = authenticate();
            createDefaultTimes(cookie);
            createDefaultThemes(cookie);

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
                    .when().delete("/api/v1/admin/times/1")
                    .then().log().all()
                    .statusCode(409)
                    .body("errorCode", is("RESERVATION_TIME409_001"));
        }
    }

    @Nested
    class 인증_실패 {

        @Test
        void 비로그인_상태로_예약시간_생성시_401을_반환한다() {
            RestAssured.given().contentType(ContentType.JSON)
                    .body(timeParams())
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 비로그인_상태로_예약시간_삭제시_401을_반환한다() {
            RestAssured.given().log().all()
                    .when().delete("/api/v1/admin/times/1")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 잘못된_토큰으로_예약시간_생성시_401을_반환한다() {
            RestAssured.given().log().all()
                    .auth().oauth2("invalid.jwt.token")
                    .contentType(ContentType.JSON)
                    .body(timeParams())
                    .when().post("/api/v1/admin/times")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_003"));
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

    private void createDefaultTimes(String cookie) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", cookie)
                .body(time)
                .when().post("/api/v1/admin/times")
                .then().statusCode(201);

        Map<String, String> time2 = new HashMap<>();
        time2.put("startAt", "11:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", cookie)
                .body(time2)
                .when().post("/api/v1/admin/times")
                .then().statusCode(201);

        Map<String, String> time3 = new HashMap<>();
        time3.put("startAt", "12:00");

        RestAssured.given().contentType(ContentType.JSON)
                .header("Cookie", cookie)
                .body(time3)
                .when().post("/api/v1/admin/times")
                .then().statusCode(201);
    }

    private void createDefaultThemes(String cookie) {
        Map<String, Object> themeParams = new HashMap<>();
        themeParams.put("name", "이든의 공포 하우스");
        themeParams.put("description", "이든이 귀신으로 나옴");
        themeParams.put("imgUrl", "https://images.example.com/themes/horror-house.jpg");
        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(themeParams)
                .when().post("/api/v1/admin/themes")
                .then().statusCode(201);

        Map<String, Object> themeParams2 = new HashMap<>();
        themeParams2.put("name", "정콩이의 방탈출");
        themeParams2.put("description", "니는 못나간다");
        themeParams2.put("imgUrl", "https://images.example.com/themes/jungkong-room.jpg");

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(themeParams2)
                .when().post("/api/v1/admin/themes")
                .then().statusCode(201);
    }

    private Map<String, Object> reservationParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1L);
        params.put("themeId", 1L);
        params.put("storeId", 1L);
        return params;
    }

    private Map<String, Object> timeParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("startAt", "10:00");
        return params;
    }
}
