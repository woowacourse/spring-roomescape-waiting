package roomescape.acceptance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.response.Response;
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

    @Test
    @DisplayName("동일한 예약이 존재하지 않는 상황에, 예약 요청을 보내면, resolved 예약된다")
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

    @Test
    @DisplayName("동일한 예약이 존재하는 상황에, 예약 요청을 보내면, 예약 대기 상태가 된다")
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

    @Test
    @DisplayName("내가 예약한 상태에서, 예약 요청을 보내면, 예약이 거절된다")
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

    @Test
    @DisplayName("내가 예약 대기한 상태에서, 예약 요청을 보내면, 예약이 거절된다")
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

    @Disabled
    @Test
    @DisplayName("예약을 취소한 상태에서, 예약 요청을 보내면, 예약된다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_canceledReservation_then_addReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        // when
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().delete("/reservations")
                .then().log().all().statusCode(204);

        Response response = given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .extract().response();

        // then
        // 예약 상태인지 확인하는 로직이 필요하다
    }

    @Disabled
    @Test
    @DisplayName("뒤에 예약 대기가 존재하는 상태에서, 예약 대기를 취소하고 다시 예약 요청을 보내면, 예약 대기 상태가 된다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_canceledWaitingReservation_then_addWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
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

        // when
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().delete("/reservations")
                .then().log().all().statusCode(204);

        Response response = given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .contentType("application/json")
                .body(requestBody)
                .when().post("/reservations")
                .then().log().all().statusCode(201)
                .extract().response();

        // then
        // 예약 상태인지 확인하는 로직이 필요하다
    }

    @Disabled
    @Test
    @DisplayName("이미 지난 시간에 대한 예약 요청을 보내면, 예약이 거절된다")
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

    @Disabled
    @Test
    @DisplayName("존재하지 않는 시간에 대한 예약 요청을 보내면, 예약이 거절된다")
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

    @Disabled
    @Test
    @DisplayName("존재하지 않는 테마에 대한 예약 요청을 보내면, 예약이 거절된다")
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

    @Disabled
    @Test
    @DisplayName("존재하지 않는 회원에 대한 예약 요청을 보내면, 예약이 거절된다")
    @Sql(value = {"/test-data/themes.sql", "/test-data/times.sql"})
    void when_noMemberReservation_then_rejectReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        Long memberId = 100L;
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

    @Disabled
    @Test
    @DisplayName("예약이 존재하지 않는 상황에서, 예약을 취소 요청을 보내면, 요청을 무시한다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql"})
    void when_noReservation_then_throwException() {
        // given
        Long reservationId = 1L;

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/" + reservationId)
                .then().log().all().statusCode(204);
    }

    @Disabled
    @Test
    @DisplayName("과거 resolved 예약에 대해 취소 요청을 보내면, 요청을 무시한다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/past-reservations.sql"})
    void when_pastTimeReservation_then_nothingHappens() {
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

    @Disabled
    @Test
    @DisplayName("resolved 예약과 waiting 예약이 모두 있는 상태에서, 모든 예약을 조회하면, resolved 예약과 waiting 예약을 모두 반환한다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql", "/test-data/waiting-reservations.sql"})
    void when_getReservations_then_returnReservations() {
        // when
        Response response = given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().get("/reservations")
                .then().log().all().statusCode(200)
                .extract().response();

        // then
        // 예약과 예약 대기 상태가 적절한지 확인하는 로직이 필요하다
        // 정렬과 페이징 처리가 필요하다
    }

    @Disabled
    @Test
    @DisplayName("과거의 resolved 예약과 waiting 예약은 조회되지 않는다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/past-reservations.sql", "/test-data/past-waiting-reservations.sql"})
    void when_getReservations_then_doesNotReturnPastReservations() {
        // when
        Response response = given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().get("/reservations")
                .then().log().all().statusCode(200)
                .extract().response();

        // then
        // 지난 예약과 예약 대기 상태가 조회되지 않는지 확인하는 로직이 필요하다
    }

    @Disabled
    @Test
    @DisplayName("내 waiting 예약이 존재해야만, waiting 예약을 삭제할 수 있다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/waiting-reservations.sql"})
    void when_myWaitingReservationExists_then_deleteWaitingReservation() {
        // given
        Long themeId = 1L;
        Long timeId = 1L;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String requestBody = String.format("{\"themeId\":%d, \"date\":\"%s\", \"timeId\":%d}",
                themeId, tomorrow, timeId);

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/1?waiting=true")
                .then().log().all().statusCode(204);
    }

    @Disabled
    @Test
    @DisplayName("내 waiting 예약이 존재하지 않으면, waiting 예약을 삭제할 수 없다")
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
                .when().delete("/reservations/1?waiting=true")
                .then().log().all().statusCode(400);
    }

    @Disabled
    @Test
    @DisplayName("다른 사람의 waiting 예약을 삭제할 수 없다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/waiting-reservations.sql"})
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

        // when, then
        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/1?waiting=true")
                .then().log().all().statusCode(400);
    }

    @Disabled
    @Test
    @DisplayName("resolved 예약으로 전환되면, pending 예약 취소 요청을 할 수 없다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/reservations-details.sql"})
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
                .when().delete("/reservations/1?waiting=false")
                .then().log().all().statusCode(204);

        given().log().all()
                .cookie("token", getToken("mangcho@woowa.net", "password"))
                .when().delete("/reservations/1?waiting=true")
                .then().log().all().statusCode(400);
    }

    @Disabled
    @Test
    @DisplayName("waiting 예약에 대해, resolved 예약 취소 요청을 할 수 없다")
    @Sql(value = {"/test-data/members.sql", "/test-data/themes.sql", "/test-data/times.sql",
            "/test-data/waiting-reservations.sql"})
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
