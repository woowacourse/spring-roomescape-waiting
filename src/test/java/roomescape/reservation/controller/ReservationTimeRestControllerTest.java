package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationTimeRestControllerTest {

    @Autowired
    private ReservationTimeRestController reservationTimeRestController;

    @Test
    void 예약_가능한_시간을_목록에_추가한다() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "20:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    void 예약_가능한_시간을_목록에서_조회한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(10));
    }

    @Test
    void 예약_가능한_시간을_목록에서_삭제한다() {
        final Map<String, String> params = new HashMap<>();
        params.put("startAt", "20:00");

        final Integer id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().path("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/times/{id}", id)
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 컨트롤러는_JdbcTemplate_타입의_필드를_갖고_있지_않다() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationTimeRestController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
