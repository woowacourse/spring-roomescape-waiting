package roomescape.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.controller.ReservationController;
import roomescape.reservationwaiting.controller.ReservationWaitingController;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class Roomescapee2eTest {

    @LocalServerPort
    int port;

    @Autowired
    private ReservationWaitingController reservationWaitingController;

    @Autowired
    private ReservationController reservationController;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("대기 생성 -> 조회 -> 순번 확인")
    void 대기_순번_확인_테스트() {
        //예약 대기 3개 생성
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(4))
                .body("name", equalTo("현미밥"))
                .body("reservationId", equalTo(12));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥2", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(5))
                .body("name", equalTo("현미밥2"))
                .body("reservationId", equalTo(12));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥3", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(6))
                .body("name", equalTo("현미밥3"))
                .body("reservationId", equalTo(12));

        //이름으로 조회해 순번이 올바르게 나오는지 검증
        RestAssured.given().log().all()
                .queryParam("name", "현미밥3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(6))
                .body("[0].turn", equalTo(4));

        //가장 먼저 등록한 사람은 순번이 1번이다
        RestAssured.given().log().all()
                .queryParam("name", "user1")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(2))
                .body("[0].turn", equalTo(1));
    }

    @Test
    @DisplayName("대기 취소 후 순번 재정렬")
    void 순번_정렬_테스트() {
        //예약 대기 3개 생성
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(4))
                .body("name", equalTo("현미밥"))
                .body("reservationId", equalTo(12));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥2", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(5))
                .body("name", equalTo("현미밥2"))
                .body("reservationId", equalTo(12));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "현미밥3", "reservationId", 12))
                .when().post("/waitings")
                .then().log().all()
                .statusCode(201)
                .body("id", equalTo(6))
                .body("name", equalTo("현미밥3"))
                .body("reservationId", equalTo(12));

        //대기 1번인 사람이 취소
        RestAssured.given().log().all()
                .when().delete("/waitings/2")
                .then().log().all()
                .statusCode(204);
        
        //이름으로 조회해 순번이 올바르게 나오는지 검증
        RestAssured.given().log().all()
                .queryParam("name", "현미밥3")
                .when().get("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", equalTo(6))
                .body("[0].turn", equalTo(3));
    }
}
