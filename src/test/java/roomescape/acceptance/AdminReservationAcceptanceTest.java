package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;
import roomescape.fixture.Scenario;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationAcceptanceTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String MANAGER_NAME = "관리자";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long managedStoreId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        long managerId = DbFixtures.insertManager(jdbcTemplate, MANAGER_NAME);
        managedStoreId = DbFixtures.insertStore(jdbcTemplate, "기본매장");
        DbFixtures.assignManager(jdbcTemplate, managedStoreId, managerId);
    }

    @Test
    @DisplayName("GET /admin/reservations - 목록을 조회한다")
    void getReservations() {
        Scenario.reservation(jdbcTemplate).member("브라운").onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", equalTo("브라운"));
    }

    @Test
    @DisplayName("GET /admin/reservations - name으로 필터링한다")
    void getReservationsFilteredByName() {
        Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(-6).toString())
                .onStore(managedStoreId).save();
        Scenario.reservation(jdbcTemplate).member("다른사람").date(Fixtures.daysFromNow(-5).toString())
                .onStore(managedStoreId).save();
        Scenario.reservation(jdbcTemplate).member("브라운").date(Fixtures.daysFromNow(-4).toString())
                .onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations?name=브라운")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("reservations.name", org.hamcrest.Matchers.everyItem(equalTo("브라운")));
    }

    @Test
    @DisplayName("GET /admin/reservations - name이 빈 문자열이면 400과 메시지를 반환한다")
    void getReservationsReturns400WhenNameIsBlank() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations?name=")
                .then().log().all()
                .statusCode(400)
                .body("code", equalTo("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("GET /admin/reservations - MEMBER 토큰이면 403과 메시지를 반환한다")
    void getReservationsReturns403WithMemberToken() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403)
                .body("code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("GET /admin/reservations - 토큰이 없으면 401과 메시지를 반환한다")
    void getReservationsReturns401WithoutToken() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401)
                .body("code", equalTo("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 예약을 취소한다")
    void deleteReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운")
                .date(Fixtures.daysFromNow(1).toString()).onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().post("/admin/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 확정 예약을 취소하면 같은 슬롯의 대기가 승격된다")
    void promotesWaitingWhenReservedIsCanceled() {
        String date = Fixtures.daysFromNow(1).toString();
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate)
                .member("예약자").date(date).onStore(managedStoreId).save();
        Scenario.ExistingReservation waiting = Scenario.waitingReservation(jdbcTemplate)
                .member("대기자").date(date)
                .onTheme(reserved.themeId()).onTime(reserved.timeId()).onStore(reserved.storeId())
                .save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().post("/admin/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(200);

        RestAssured.given().log().all()
                .header(AUTHORIZATION, waiting.bearer())
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("waitingReservations.size()", is(0));
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 과거 예약이면 422와 메시지를 반환한다")
    void deleteReservationReturns422WhenReservationIsPast() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운")
                .date(Fixtures.daysFromNow(-1).toString()).onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().post("/admin/reservations/" + reserved.reservationId() + "/cancel")
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("PAST_RESERVATION_MODIFICATION"));
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 과거 예약을 삭제한다")
    void deletePastReservation() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운")
                .date(Fixtures.daysFromNow(-1).toString()).onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 아직 지나지 않은 예약이면 422와 메시지를 반환한다")
    void deleteReservationReturns422WhenReservationIsNotPast() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운")
                .date(Fixtures.daysFromNow(1).toString()).onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(422)
                .body("code", equalTo("NON_PAST_RESERVATION_DELETION"));
    }

    @Test
    @DisplayName("GET /admin/reservations - 담당하는 매장의 예약만 반환한다")
    void getReservationsReturnsOnlyManagedStoreReservations() {
        Scenario.reservation(jdbcTemplate).member("브라운").onStore(managedStoreId).save();
        insertReservationInOtherStore(Fixtures.daysFromNow(1).toString());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", equalTo("브라운"));
    }

    @Test
    @DisplayName("POST /admin/reservations/{id}/cancel - 담당하지 않는 매장 예약이면 403과 메시지를 반환한다")
    void deleteReservationReturns403WhenStoreIsNotManaged() {
        long reservationId = insertReservationInOtherStore(Fixtures.daysFromNow(1).toString());

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().post("/admin/reservations/" + reservationId + "/cancel")
                .then().log().all()
                .statusCode(403)
                .body("code", equalTo("STORE_MANAGEMENT_FORBIDDEN"));
    }

    @Test
    @DisplayName("DELETE /admin/reservations/{id} - 없는 id면 404과 메시지를 반환한다")
    void deleteReservationReturns404WhenIdDoesNotExist() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/9999")
                .then().log().all()
                .statusCode(404)
                .body("code", equalTo("RESOURCE_NOT_FOUND"));
    }

    private long insertReservationInOtherStore(String date) {
        long otherStoreId = DbFixtures.insertStore(jdbcTemplate, "잠실점");
        long userId = DbFixtures.insertMember(jdbcTemplate, "타매장유저");
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "타매장테마");
        long timeId = DbFixtures.insertTime(jdbcTemplate, "11:00");
        return DbFixtures.insertReservationInStore(jdbcTemplate, userId, themeId, date, timeId, otherStoreId);
    }

    private String managerBearer() {
        return DbFixtures.managerBearer(jdbcTemplate, MANAGER_NAME);
    }
}
