package roomescape.reservationhistory;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

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
public class ReservationHistoryControllerTest {

    private static final String BROWN_EMAIL = "brown@email.com";
    private static final String JEONGKONG_EMAIL = "jeongkong@email.com";
    private static final String PASSWORD = "password";

    private static final String INSERT_DEFAULT_STORE_SQL = """
            INSERT INTO store (id, name)
            VALUES (1, '강남점');
            """;

    private static final String INSERT_TWO_MEMBERS_SQL = """
            INSERT INTO member (id, email, password, name, role)
            VALUES (1, 'brown@email.com', 'password', '브라운', 'USER'),
                   (2, 'jeongkong@email.com', 'password', '정콩이', 'USER');
            """;

    private static final String INSERT_BROWN_HISTORIES_SQL = """
            INSERT INTO reservation_history
                (reservation_id, member_id, date, time_id, theme_id, store_id, action, actor_id)
            VALUES (1, 1, '2026-12-01', 1, 1, 1, 'CANCELED', 1),
                   (2, 1, '2026-12-02', 1, 1, 1, 'CREATED', 1);
            """;

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_BROWN_HISTORIES_SQL
    })
    void 본인의_예약_이력을_조회한다() {
        String cookie = loginAndGetCookie(BROWN_EMAIL);

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/api/v1/reservation-history")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("memberId", hasItem(1));
    }

    @Test
    @Sql(statements = {
            INSERT_DEFAULT_STORE_SQL,
            INSERT_TWO_MEMBERS_SQL,
            INSERT_BROWN_HISTORIES_SQL
    })
    void 본인이_아닌_이력은_조회되지_않는다() {
        String cookie = loginAndGetCookie(JEONGKONG_EMAIL);

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().get("/api/v1/reservation-history")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 비로그인으로_조회하면_401_AUTH401_002를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/api/v1/reservation-history")
                .then().log().all()
                .statusCode(401)
                .body("errorCode", is("AUTH401_002"));
    }

    @Test
    void 잘못된_토큰으로_조회하면_401_AUTH401_003을_반환한다() {
        RestAssured.given().log().all()
                .auth().oauth2("invalid.jwt.token")
                .when().get("/api/v1/reservation-history")
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
