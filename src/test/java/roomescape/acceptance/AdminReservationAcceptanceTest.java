package roomescape.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.fixture.DbFixtures;
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
    void GET_admin_reservations_목록을_조회한다() {
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
    void GET_admin_reservations_name으로_필터링한다() {
        Scenario.reservation(jdbcTemplate).member("브라운").date("2026-05-01").onStore(managedStoreId).save();
        Scenario.reservation(jdbcTemplate).member("다른사람").date("2026-05-02").onStore(managedStoreId).save();
        Scenario.reservation(jdbcTemplate).member("브라운").date("2026-05-03").onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations?name=브라운")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2))
                .body("reservations.name", org.hamcrest.Matchers.everyItem(equalTo("브라운")));
    }

    @Test
    void GET_admin_reservations_name이_빈_문자열이면_400과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations?name=")
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("name은(는) 최소 1자 이상이어야 합니다."));
    }

    @Test
    void GET_admin_reservations_MEMBER_토큰이면_403과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, DbFixtures.memberBearer(jdbcTemplate, "브라운"))
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("접근 권한이 없습니다. 관리자만 이용할 수 있습니다."));
    }

    @Test
    void GET_admin_reservations_토큰이_없으면_401과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401)
                .body("message", equalTo("인증이 필요합니다. 로그인 후 이용해주세요."));
    }

    @Test
    void DELETE_admin_reservations_id_예약을_삭제한다() {
        Scenario.ExistingReservation reserved = Scenario.reservation(jdbcTemplate).member("브라운").onStore(managedStoreId).save();

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/" + reserved.reservationId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void GET_admin_reservations_담당하는_매장의_예약만_반환한다() {
        Scenario.reservation(jdbcTemplate).member("브라운").onStore(managedStoreId).save();
        insertReservationInOtherStore("2026-05-09");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", equalTo("브라운"));
    }

    @Test
    void DELETE_admin_reservations_담당하지_않는_매장_예약이면_403과_메시지를_반환한다() {
        long reservationId = insertReservationInOtherStore("2026-05-09");

        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/" + reservationId)
                .then().log().all()
                .statusCode(403)
                .body("message", equalTo("본인이 관리하는 매장의 예약만 관리할 수 있습니다."));
    }

    @Test
    void DELETE_admin_reservations_id_없는_id면_404과_메시지를_반환한다() {
        RestAssured.given().log().all()
                .header(AUTHORIZATION, managerBearer())
                .when().delete("/admin/reservations/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", equalTo("예약을(를) 찾을 수 없습니다. id=9999"));
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
