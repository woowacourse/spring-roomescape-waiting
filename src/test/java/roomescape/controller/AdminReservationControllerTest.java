package roomescape.controller;

import static org.hamcrest.core.Is.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationControllerTest {

    @DisplayName("모든 사용자의 예약 내역이 모두 조회되어야한다.")
    @Test
    void 관리자_예약_조회_API() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(19))
                .body("[0].id", is(1))
                .body("[0].name", is("김철수"))
                .body("[0].date", is("2026-04-29"))
                .body("[0].themeName", is("공포의 저택"))
                .body("[0].time", is("12:00"));

    }

}
