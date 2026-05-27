package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql("/cleanup.sql")
@SqlMergeMode(MergeMode.MERGE)
public class AdminStoreReservationControllerTest {

    private static final String INSERT_TWO_STORES_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점'),
                   (2, '홍대점');
            """;

    private static final String INSERT_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name, role, store_id)
            VALUES (1, 'gangnam@email.com', 'password', '강남매니저', 'MANAGER', 1),
                   (2, 'hongdae@email.com', 'password', '홍대매니저', 'MANAGER', 2),
                   (3, 'user@email.com', 'password', '일반유저', 'USER', NULL);
            """;

    private static final String INSERT_SINGLE_TIME_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00');
            """;

    private static final String INSERT_TWO_TIMES_SQL = """
            INSERT INTO reservation_time (id, start_at)
            VALUES (1, '10:00'),
                   (2, '11:00');
            """;

    private static final String INSERT_SINGLE_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg');
            """;

    private static final String INSERT_TWO_RESERVATIONS_SQL = """
            INSERT INTO reservation (id, member_id, date, time_id, theme_id, store_id)
            VALUES (1, 3, '2026-12-01', 1, 1, 1),
                   (2, 3, '2026-12-02', 1, 1, 2);
            """;

    private static final String GANGNAM_MANAGER_EMAIL = "gangnam@email.com";
    private static final String HONGDAE_MANAGER_EMAIL = "hongdae@email.com";
    private static final String REGULAR_USER_EMAIL = "user@email.com";
    private static final String PASSWORD = "password";

    @Nested
    class 매장_예약_목록_조회 {

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_SINGLE_TIME_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 매니저는_자기_매장의_예약만_조회한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/admin/store/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("id", hasItem(1))
                    .body("id", not(hasItem(2)));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_SINGLE_TIME_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 다른_매장_매니저는_자기_매장의_예약만_본다() {
            String cookie = loginAndGetCookie(HONGDAE_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/admin/store/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("id", hasItem(2))
                    .body("id", not(hasItem(1)));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL
        })
        void 일반_사용자가_조회하면_403_AUTH403_001을_반환한다() {
            String cookie = loginAndGetCookie(REGULAR_USER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().get("/api/v1/admin/store/reservations")
                    .then().log().all()
                    .statusCode(403)
                    .body("errorCode", is("AUTH403_001"));
        }

        @Test
        void 비로그인으로_조회하면_401_AUTH401_002를_반환한다() {
            RestAssured.given().log().all()
                    .when().get("/api/v1/admin/store/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }

        @Test
        void 잘못된_토큰으로_조회하면_401_AUTH401_003을_반환한다() {
            RestAssured.given().log().all()
                    .auth().oauth2("invalid.jwt.token")
                    .when().get("/api/v1/admin/store/reservations")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_003"));
        }
    }

    @Nested
    class 매장_예약_삭제 {

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_SINGLE_TIME_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 매니저가_자기_매장의_예약을_삭제하면_204를_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_SINGLE_TIME_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 다른_매장의_예약을_삭제하려_하면_403_AUTH403_002를_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/store/reservations/2")
                    .then().log().all()
                    .statusCode(403)
                    .body("errorCode", is("AUTH403_002"));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_SINGLE_TIME_SQL,
                INSERT_SINGLE_THEME_SQL
        })
        void 존재하지_않는_예약을_삭제하려_하면_404를_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/store/reservations/999")
                    .then().log().all()
                    .statusCode(404)
                    .body("errorCode", is("RESERVATION404_001"));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL
        })
        void 일반_사용자가_삭제하려_하면_403_AUTH403_001을_반환한다() {
            String cookie = loginAndGetCookie(REGULAR_USER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .when().delete("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(403)
                    .body("errorCode", is("AUTH403_001"));
        }

        @Test
        void 비로그인으로_삭제하려_하면_401_AUTH401_002를_반환한다() {
            RestAssured.given().log().all()
                    .when().delete("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }
    }

    @Nested
    class 매장_예약_변경 {

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 매니저가_자기_매장의_예약을_변경하면_200을_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(updateParams("2026-12-15", 2L))
                    .when().patch("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(200)
                    .body("id", is(1));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 다른_매장의_예약을_변경하려_하면_403_AUTH403_002를_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(updateParams("2026-12-15", 2L))
                    .when().patch("/api/v1/admin/store/reservations/2")
                    .then().log().all()
                    .statusCode(403)
                    .body("errorCode", is("AUTH403_002"));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_SINGLE_THEME_SQL
        })
        void 존재하지_않는_예약을_변경하려_하면_404를_반환한다() {
            String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(updateParams("2026-12-15", 2L))
                    .when().patch("/api/v1/admin/store/reservations/999")
                    .then().log().all()
                    .statusCode(404)
                    .body("errorCode", is("RESERVATION404_001"));
        }

        @Test
        @Sql(statements = {
                INSERT_TWO_STORES_SQL,
                INSERT_MEMBERS_SQL,
                INSERT_TWO_TIMES_SQL,
                INSERT_SINGLE_THEME_SQL,
                INSERT_TWO_RESERVATIONS_SQL
        })
        void 일반_사용자가_변경하려_하면_403_AUTH403_001을_반환한다() {
            String cookie = loginAndGetCookie(REGULAR_USER_EMAIL);

            RestAssured.given().log().all()
                    .header("Cookie", cookie)
                    .contentType(ContentType.JSON)
                    .body(updateParams("2026-12-15", 2L))
                    .when().patch("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(403)
                    .body("errorCode", is("AUTH403_001"));
        }

        @Test
        void 비로그인으로_변경하려_하면_401_AUTH401_002를_반환한다() {
            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(updateParams("2026-12-15", 2L))
                    .when().patch("/api/v1/admin/store/reservations/1")
                    .then().log().all()
                    .statusCode(401)
                    .body("errorCode", is("AUTH401_002"));
        }
    }

    private Map<String, Object> updateParams(String date, Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date);
        params.put("timeId", timeId);
        return params;
    }

    private String loginAndGetCookie(String email) {
        return RestAssured
                .given()
                .param("email", email)
                .param("password", PASSWORD)
                .when().post("/api/v1/auth/login")
                .then()
                .extract().header("Set-Cookie")
                .split(";")[0];
    }
}
