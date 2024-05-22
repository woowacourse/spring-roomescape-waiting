package roomescape.acceptance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SqlMergeMode(MergeMode.MERGE)
@Sql("/init/truncate.sql")
class MemberReservationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String getToken(String email, String password) {
        String requestBody = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);

        return given().log().all()
                .contentType("application/json")
                .body(requestBody)
                .when().post("/login")
                .then().log().all().statusCode(200)
                .extract().cookie("token");
    }

    @DisplayName("동일한 예약이 존재하지 않는 상황에, 예약 요청을 보내면, resolved 예약된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_noReservation_then_addReservation() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":1, \"date\":\"%s\", \"timeId\":1}", tomorrow);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .and()
                .body("status", equalTo("RESERVED"));
    }

    @DisplayName("동일한 예약이 존재하는 상황에, 예약 요청을 보내면, 예약 대기 상태가 된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_reservationExists_then_addWaitingReservation() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        Long timeId = 1L;
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("RESERVED"));

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("status", equalTo("WAITING"));
    }

    @DisplayName("내가 예약한 상태에서, 예약 요청을 보내면, 예약이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_myReservationExists_then_rejectReservation() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        Long timeId = 1L;
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all().statusCode(201);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("내가 예약 대기한 상태에서, 예약 요청을 보내면, 예약이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_myWaitingReservationExists_then_rejectReservation() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Long themeId = 1L;
        Long timeId = 1L;
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예약 대기를 취소한 상태에서, 예약 대기 요청을 보내면, 예약 대기된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_canceledReservation_then_addReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("RESERVED"));

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("WAITING"));

        // when
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().delete("/reservations/" + 2)
                .then().log().all().statusCode(204);

        // then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("WAITING"));
    }

    @DisplayName("다른 사람의 예약 대기가 존재하는 상태에서, 내가 다른 사람의 예약 대기 취소 요청을 보내면, 요청은 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_anotherWaitingReservationExists_then_canNotDeleteOthersWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        given().log().all()
                .cookie("token", getToken("picachu@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/2")
                .then().log().all().statusCode(400);
    }

    @DisplayName("뒤에 예약 대기가 존재하는 상태에서, 예약 대기를 취소하고 다시 예약 요청을 보내면, 예약 대기 상태가 된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_canceledWaitingReservation_then_addWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("RESERVED"));

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("WAITING"));

        given().log().all()
                .cookie("token", getToken("picachu@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("WAITING"));

        // when
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .when().delete("/reservations/" + 2)
                .then().log().all().statusCode(204);

        // then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .body("status", equalTo("WAITING"));
    }

    @DisplayName("이미 지난 시간에 대한 예약 요청을 보내면, 예약이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql"})
    void when_pastTimeReservation_then_rejectReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, yesterday, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(400);
    }

    @DisplayName("존재하지 않는 시간에 대한 예약 요청을 보내면, 예약이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql"})
    void when_noTimeReservation_then_rejectReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 100L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(400);
    }

    @DisplayName("존재하지 않는 테마에 대한 예약 요청을 보내면, 예약이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/times.sql"})
    void when_noThemeReservation_then_rejectReservation() {
        // given
        Long themeId = 100L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(400);
    }

    @DisplayName("예약이 존재하지 않는 상황에서, 예약을 취소 요청을 보내면, 요청을 무시한다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_noReservation_then_throwException() {
        // given
        Long reservationId = 1L;

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/" + reservationId)
                .then().log().all().statusCode(400);
    }

    @DisplayName("과거 예약에 대해 취소 요청을 보내면, 요청을 무시한다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_cancelPastTimeReservation_then_nothingHappens() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, yesterday, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/1")
                .then().log().all().statusCode(400);
    }

    @DisplayName("예약과 예약 대기가 모두 있는 상태에서, 모든 예약을 조회하면, 예약과 예약 대기를 모두 반환한다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql"})
    void when_getReservations_then_returnReservations() {
        // when, then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().get("/reservations-mine")
                .then().log().all().statusCode(200)
                .assertThat()
                .body("size()", is(11))
                .body("findAll { it.status == '예약' }.size()", is(8))
                .body("findAll { it.status == '1번째 예약' }.size()", is(3));
    }

    @Disabled
    @DisplayName("과거의 예약과 예약 대기는 조회되지 않는다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql", "/test-data/reservations.sql", "/test-data/past-reservations.sql"})
    void when_getReservations_then_doesNotReturnPastReservations() {
        // when, then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().get("/reservations-mine")
                .then().log().all().statusCode(200)
                .assertThat()
                .body("size()", is(11))
                .body("findAll { it.status == '예약' }.size()", is(8))
                .body("findAll { it.status == '1번째 예약' }.size()", is(3));
    }

    @DisplayName("내 예약 대기가 존재하는 경우에, 예약 대기 삭제 요청을 하면, 삭제된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_myWaitingReservationExists_then_deleteWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/" + 1)
                .then().log().all().statusCode(400);
    }

    @DisplayName("내 예약 대기가 존재하지 않으면, 예약 대기를 삭제할 수 없다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_noWaitingReservation_then_canNotDeleteWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/" + 1)
                .then().log().all().statusCode(400);
    }

    @DisplayName("예약으로 전환된 상태면, 예약 취소 요청하면, 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_reservationStatusChangedIntoResolved_then_canNotDeleteWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        // when, then
        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .when().delete("/admin/reservations/" + 1)
                .then().log().all().statusCode(204);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/" + 1)
                .then().log().all().statusCode(400);
    }

    @DisplayName("예약 대기가 존재하는 상태에서, 해당 예약 대기에 취소 요청을 보내면, 요청이 거절된다")
    @Test
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_waitingReservationExists_then_canNotDeleteResolvedReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mrmrmrmr@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/1?waiting=true")
                .then().log().all().statusCode(400);
    }

}
