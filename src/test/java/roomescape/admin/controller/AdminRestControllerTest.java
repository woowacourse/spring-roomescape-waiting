package roomescape.admin.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql("classpath:data.sql")
class AdminRestControllerTest {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "yebink@email.com", "password", "1234"))
                .when().post("/login").getCookie("token");
    }

    @Test
    void 어드민이_예약을_생성한다() {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.of(2025, 12, 12));
        params.put("themeId", 1);
        params.put("timeId", 1);
        params.put("memberId", 1);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 어드민이_조건에_맞는_예약을_조회한다() {
        // given
        final String adminToken = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", "yebink@email.com", "password", "1234"))
                .when().post("/login").getCookie("token");

        final String dateFrom = LocalDate.now().minusDays(1).toString();
        final String dateTo = LocalDate.now().plusDays(1).toString();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .queryParam("themeId", 1)
                .queryParam("memberId", 1)
                .queryParam("dateFrom", dateFrom)
                .queryParam("dateTo", dateTo)
                .when().get("/admin/searchable-reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", greaterThanOrEqualTo(0));
    }
}
