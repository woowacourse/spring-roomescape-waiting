package roomescape.acceptance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SqlMergeMode(MergeMode.MERGE)
@Sql("/init/truncate.sql")
class AdminReservationTest {

    private static final Map<String, String> TOKEN_CACHE = new HashMap<>();

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String getToken(String email, String password) {
        if (TOKEN_CACHE.containsKey(email)) {
            return TOKEN_CACHE.get(email);
        }

        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);

        String token = given().log().all()
                .contentType("application/json")
                .body(requestBody)
                .when().post("/login")
                .then().log().all().statusCode(200)
                .extract().cookie("token");

        TOKEN_CACHE.put(email, token);
        return token;
    }

    @DisplayName("어드민이 예약을 추가하면, 예약이 추가된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_adminAddReservation_then_reservationAdded() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        Long timeId = 1L;
        Long memberId = 2L;
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d, \"memberId\":%d}",
                themeId, tomorrow, timeId, memberId);

        // when
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));
    }

    @DisplayName("예약 대기가 있는 상태에서, 어드민이 예약으로 전환 요청을 보내면, 예약 상태로 바뀐다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_adminApproveWaiting_then_reservation() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        Long timeId = 1L;
        Long memberId = 2L;
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d, \"memberId\":%d}",
                themeId, tomorrow, timeId, memberId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));

        memberId = 1L;
        requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d, \"memberId\":%d}",
                themeId, tomorrow, timeId, memberId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("WAITING"));

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(204);

        // when, then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().post("/admin/reservations/" + 2)
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("RESERVED"));
    }

    @DisplayName("어드민이 예약을 조회하면, 예약 목록을 조회할 수 있다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql"})
    void when_adminGetReservations_then_reservations() {
        // when, then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().get("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(14));
    }

    @DisplayName("멤버가 예약한 상태에서, 어드민이 취소하면 요청을 보내면, 예약은 취소된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_adminCancelReservation_then_reservationCanceled() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":1, \"date\":\"%s\", \"timeId\":1}", tomorrow);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));

        // when
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(204);

        // then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().get("/reservations-mine")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @DisplayName("예약이 취소된 상태에서, 어드민이 예약 상태로 변경하면, 예외가 발생한다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_adminChangeCanceledReservation_then_exceptionThrown() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":1, \"date\":\"%s\", \"timeId\":1}", tomorrow);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(204);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(400);
    }

    @DisplayName("예약과 대기가 존재하는 상태에서, 예약을 삭제하고 대기를 승인하면, 예약 상태로 변경된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_adminApproveReservation_then_reserved() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":1, \"date\":\"%s\", \"timeId\":1}", tomorrow);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));

        given().log().all()
                .cookie("token", getToken("picachu@woowa.net", "password"))
                .body(requestBody).contentType("application/json")
                .when().post("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .body("status", equalTo("WAITING"));

        // when
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(204);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().post("/admin/reservations/" + 2)
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("RESERVED"));
    }

    @DisplayName("예약을 취소하면, 조회되지 않는다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql"})
    void when_cancelReservation_then_notFound() {
        // when
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all()
                .assertThat()
                .statusCode(204);

        // then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().get("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("size()", equalTo(13));
    }
}
