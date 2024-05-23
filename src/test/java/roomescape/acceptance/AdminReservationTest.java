package roomescape.acceptance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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
class AdminReservationTest {

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
}
