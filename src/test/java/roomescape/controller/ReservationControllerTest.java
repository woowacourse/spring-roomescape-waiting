package roomescape.controller;

import static org.hamcrest.core.Is.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {
    @Autowired
    private ReservationController reservationController;

    @DisplayName("사용자 예약 추가 API")
    @Test
    void 사용자_예약_추가_API() {
        Map<String,Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2026-05-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("사용자 예약 삭제 API")
    @Test
    void 사용자_예약_삭제_API(){
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("id",1)
                .when().delete("/reservations/{id}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("사용자 예약 조회 API")
    @Test
    void 사용자_예약_조회_API(){
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("username","김철수")
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].date", is("2026-04-29"))
                .body("[0].themeName", is("공포의 저택"))
                .body("[0].themeDescription", is("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다."))
                .body("[0].themeThumbnailUrl", is("https://picsum.photos/seed/haunted/400/250"))
                .body("[0].time", is("12:00"));


    }

    @DisplayName("사용자 예약 추가 - 날짜 형식 예외 테스트")
    @Test
    void 사용자_예약_추가_날짜_형식_예외_테스트() {
        Map<String,Object> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", "2024-95-05");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    }
