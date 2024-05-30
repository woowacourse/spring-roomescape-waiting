package roomescape.reservation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ADMIN;
import static roomescape.fixture.ThemeFixture.THEME_1;
import static roomescape.fixture.TimeFixture.TIME_1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.dto.LoginRequest;
import roomescape.fixture.RestAssuredTemplate;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.TimeFixture;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminReservationControllerTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("조건에 따라 예약을 조회할 수 있다.")
    @Test
    void findReservationTest() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        // 예약 추가
        ReservationCreateRequest params = new ReservationCreateRequest(MEMBER_ADMIN.getId(), date, timeId, themeId);
        ReservationResponse response = RestAssuredTemplate.create(params, cookies);

        // 예약 조회
        Map<String, String> parameters = Map.of("themeId", "1");
        List<ReservationResponse> reservationResponses = RestAssured.given().log().all()
                .cookies(cookies)
                .params(parameters)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath()
                .getList("", ReservationResponse.class);

        assertThat(reservationResponses).containsExactlyInAnyOrder(response);
    }

    @DisplayName("예약을 DB에 추가할 수 있다.")
    @Test
    void createReservationTest() {
        Cookies cookies = RestAssuredTemplate.makeUserCookie(MEMBER_ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Long themeId = RestAssuredTemplate.create(ThemeFixture.toThemeCreateRequest(THEME_1), cookies).id();
        Long timeId = RestAssuredTemplate.create(TimeFixture.toTimeCreateRequest(TIME_1), cookies).id();

        ReservationCreateRequest params = new ReservationCreateRequest(MEMBER_ADMIN.getId(), date, timeId, themeId);

        // 예약 추가
        ReservationResponse response = RestAssured.given().log().all()
                .cookies(cookies)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getObject("", ReservationResponse.class);

        // 예약 조회
        List<ReservationResponse> reservationResponses = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", ReservationResponse.class);

        assertThat(reservationResponses).containsExactlyInAnyOrder(response);
    }

    private Cookies makeAdminCookie() {
        LoginRequest request = new LoginRequest("admin@abc.com", "1234");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().detailedCookies();
    }
}
