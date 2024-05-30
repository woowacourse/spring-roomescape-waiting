package roomescape.integration;

import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import roomescape.controller.request.AdminReservationRequest;
import roomescape.controller.request.MemberLoginRequest;
import roomescape.controller.request.ReservationRequest;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/init-data.sql", "/controller-test-data.sql"})
class ReservationIntegrationTest {

    @DisplayName("예약을 조회한다.")
    @Test
    void should_get_reservations() {
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()", equalTo(8));
    }

    @DisplayName("예약을 검색한다.")
    @Test
    void should_search_reservations() {
        String cookie = createCookie("2222", "pobi@email.com");
        RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .queryParam("themeId", 1)
                .queryParam("memberId", 1)
                .queryParam("dateFrom", LocalDate.now().minusDays(1).toString())
                .queryParam("dateTo", LocalDate.now().plusDays(1).toString())
                .when().get("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()", equalTo(2));
    }

    @DisplayName("사용자가 예약을 추가할 수 있다.")
    @Test
    void should_insert_reservation_when_member_request() {
        String cookie = createCookie("1234", "sun@email.com");
        ReservationRequest request =
                new ReservationRequest(LocalDate.of(2030, 8, 5), 6L, 10L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie(cookie)
                .when().post("/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .header("Location", response -> equalTo("/reservations/" + response.path("id")));
    }

    @DisplayName("관리자가 예약을 추가할 수 있다.")
    @Test
    void should_insert_reservation_when_admin_request() {
        String cookie = createCookie("2222", "pobi@email.com");
        AdminReservationRequest request = new AdminReservationRequest(
                LocalDate.of(2030, 8, 5), 10L, 6L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie(cookie)
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .header("Location", response -> equalTo("/reservations/" + response.path("id")));
    }

    @DisplayName("존재하는 예약이라면 예약을 삭제할 수 있다.")
    @Test
    void should_delete_reservation_when_reservation_exist() {
        RestAssured.given().log().all()
                .pathParam("id", 1)
                .when().delete("/reservations/{id}")
                .then().log().all()
                .assertThat()
                .statusCode(204);
    }

    @DisplayName("로그인 정보에 따른 예약 내역을 조회한다.")
    @Test
    void should_find_member_reservation() {
        String cookie = createCookie("1234", "sun@email.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/reservations-mine")
                .then().log().all()
                .assertThat()
                .statusCode(200);
    }

    @DisplayName("대기상태의 예약을 추가할 수 있다.")
    @Test
    void should_create_waiting_reservation() {
        String cookie = createCookie("1234", "sun@email.com");
        ReservationRequest request =
                new ReservationRequest(LocalDate.of(2030, 8, 5), 6L, 10L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .cookie(cookie)
                .when().post("/reservations/waiting")
                .then().log().all()
                .assertThat()
                .statusCode(201)
                .header("Location", response -> equalTo("/reservations/" + response.path("id")));
    }

    @DisplayName("관리자는 대기상태의 예약을 조회할 수 있다.")
    @Test
    void should_find_waiting_reservations() {
        String cookie = createCookie("2222", "pobi@email.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .and()
                .body("size()", equalTo(2));
    }

    private String createCookie(String password, String email) {
        MemberLoginRequest loginRequest = new MemberLoginRequest(password, email);

        return RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().statusCode(200)
                .extract().header("Set-Cookie");
    }
}
