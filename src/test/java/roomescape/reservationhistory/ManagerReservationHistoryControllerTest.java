package roomescape.reservationhistory;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
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
public class ManagerReservationHistoryControllerTest {

    private static final String GANGNAM_MANAGER_EMAIL = "gangnam@email.com";
    private static final String HONGDAE_MANAGER_EMAIL = "hongdae@email.com";
    private static final String REGULAR_USER_EMAIL = "user@email.com";
    private static final String PASSWORD = "password";

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

    private static final String INSERT_TWO_STORE_HISTORIES_SQL = """
            INSERT INTO reservation_history
                (reservation_id, member_id, date, time_id, theme_id, store_id, status, actor_id)
            VALUES (1, 3, '2026-12-01', 1, 1, 1, 'CONFIRMED', 3),
                   (2, 3, '2026-12-02', 1, 1, 2, 'CONFIRMED', 3);
            """;

    @Test
    @Sql(statements = {
            INSERT_TWO_STORES_SQL,
            INSERT_MEMBERS_SQL,
            INSERT_TWO_STORE_HISTORIES_SQL
    })
    void 매니저는_자기_매장의_이력만_조회한다() {
        String cookie = loginAndGetCookie(GANGNAM_MANAGER_EMAIL);

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/api/v1/admin/store/reservation-history")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("storeId", hasItem(1))
                .body("storeId", not(hasItem(2)));
    }

    @Test
    @Sql(statements = {
            INSERT_TWO_STORES_SQL,
            INSERT_MEMBERS_SQL,
            INSERT_TWO_STORE_HISTORIES_SQL
    })
    void 다른_매장_매니저는_자기_매장의_이력만_본다() {
        String cookie = loginAndGetCookie(HONGDAE_MANAGER_EMAIL);

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/api/v1/admin/store/reservation-history")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("storeId", hasItem(2))
                .body("storeId", not(hasItem(1)));
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
                .when().get("/api/v1/admin/store/reservation-history")
                .then().log().all()
                .statusCode(403)
                .body("errorCode", is("AUTH403_001"));
    }

    @Test
    void 비로그인으로_조회하면_401_AUTH401_002를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/admin/store/reservation-history")
                .then().log().all()
                .statusCode(401)
                .body("errorCode", is("AUTH401_002"));
    }

    @Test
    void 잘못된_토큰으로_조회하면_401_AUTH401_003을_반환한다() {
        RestAssured.given().log().all()
                .auth().oauth2("invalid.jwt.token")
                .when().get("/api/v1/admin/store/reservation-history")
                .then().log().all()
                .statusCode(401)
                .body("errorCode", is("AUTH401_003"));
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
