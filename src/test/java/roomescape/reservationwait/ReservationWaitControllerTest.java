package roomescape.reservationwait;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
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
public class ReservationWaitControllerTest {

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
                    VALUES (1, 2, '2026-12-01', 1, 1, 1);
                    """
    })
    void 예약대기_생성에_성공하면_201을_반환한다() {
        String cookie = authenticate();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().post("/api/v1/reservations/1/waits")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1))
                .body("reservationId", is(1))
                .body("memberId", is(1));
    }

    @Test
    @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
    void 없는_예약id로_예약대기_생성시_404를_반환한다() {
        String cookie = authenticate();
        createDefaultTimesAsManager();
        createDefaultThemesAsManager();

        Map<String, Object> waitTimeParams = new HashMap<>();
        waitTimeParams.put("reservationId", 1L);
        waitTimeParams.put("memberId", 1L);

        RestAssured.given()
                .header("Cookie", cookie)
                .contentType(ContentType.JSON)
                .body(waitTimeParams)
                .when().post("/api/v1/reservations/" + 1L + "/waits")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
    void 비회원으로_예약대기_생성시_401을_반환한다() {
        String cookie = authenticate();
        createDefaultTimesAsManager();
        createDefaultThemesAsManager();

        Map<String, Object> waitTimeParams = new HashMap<>();
        waitTimeParams.put("reservationId", 1L);
        waitTimeParams.put("memberId", 1L);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(waitTimeParams)
                .when().post("/api/v1/reservations/" + 1L + "/waits")
                .then().log().all()
                .statusCode(401);
    }

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
                    VALUES (1, 2, '2026-12-01', 1, 1, 1);
                    """
    })
    void 같은_사용자가_같은_슬롯에_중복_대기하면_409를_반환한다() {
        String cookie = authenticate();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().post("/api/v1/reservations/1/waits")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().post("/api/v1/reservations/1/waits")
                .then().log().all()
                .statusCode(409)
                .body("errorCode", is("RESERVATION_WAIT409_001"));
    }

    @Test
    @Sql(statements = {INSERT_DEFAULT_STORE_SQL, INSERT_DEFAULT_MEMBER_SQL})
    void 예약대기를_삭제하면_204를_반환한다() {
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
                .when().delete("/api/v1/reservations/1/waits/mine")
                .then().log().all()
                .statusCode(204);
    }

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
                    VALUES (1, 1, '2026-12-01', 1, 1, 1);
                    """
    })
    void 본인_예약에_대기_신청시_422를_반환한다() {
        String cookie = authenticate();

        RestAssured.given().log().all()
                .header("Cookie", cookie)
                .when().post("/api/v1/reservations/1/waits")
                .then().log().all()
                .statusCode(422)
                .body("errorCode", is("RESERVATION_WAIT422_002"));
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

    private Map<String, Object> reservationParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", 1L);
        params.put("themeId", 1L);
        params.put("storeId", 1L);
        return params;
    }
}
