package roomescape.domain.waitingreservation;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/truncate.sql", "/waiting-reservation-test-data.sql"})
class WaitingReservationControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;
    }

    @Test
    void 사용자는_예약_대기를_신청한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "고래");
        params.put("dateId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/waiting-reservations")
                .then().log().all()
                .statusCode(201)
                .body("name", is("고래"))
                .body("theme.name", is("테스트테마"));
    }

    @Test
    void 중복_예약_대기_신청을_하면_409를_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "기존대기자");
        params.put("dateId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/waiting-reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 예약_가능한_시간에_대기_신청하면_409을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "고래");
        params.put("dateId", 1);
        params.put("timeId", 2);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/waiting-reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 존재하지_않는_슬롯에_대기_신청을_하면_404을_반환한다() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "고래");
        params.put("dateId", 999);
        params.put("timeId", 999);
        params.put("themeId", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when()
                .post("/waiting-reservations")
                .then().log().all()
                .statusCode(404);

    }
}
