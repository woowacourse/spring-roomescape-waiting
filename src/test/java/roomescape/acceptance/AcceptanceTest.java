package roomescape.acceptance;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("동일한 슬롯에 대해 여러 명이 예약할 경우 대기 순번이 부여되고 앞선 예약(대기) 취소 시 뒷 순번의 예약 대기가 앞당겨진다")
    void 예약_대기_시나리오() {
        // Step 1: 어드민 데이터 세팅 (테마, 시간)
        Map<String, String> themeRequest = Map.of(
                "name", "테스트 테마",
                "description", "테스트 설명",
                "thumbnailUrl", "https://example.com/thumb.jpg"
        );
        long themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(themeRequest)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("id");

        Map<String, String> timeRequest = Map.of("startAt", "10:00");
        long timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(timeRequest)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getLong("id");

        String date = "2099-12-31";

        // Step 2: 사용자 A가 예약 (waitingOrder: 0)
        Map<String, Object> reservationA = Map.of(
                "name", "사용자A",
                "date", date,
                "timeId", timeId,
                "themeId", themeId
        );
        long idA = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationA)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingOrder", is(0))
                .extract().jsonPath().getLong("id");

        // Step 3: 사용자 B가 대기 (waitingOrder: 1)
        Map<String, Object> reservationB = Map.of(
                "name", "사용자B",
                "date", date,
                "timeId", timeId,
                "themeId", themeId
        );
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationB)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingOrder", is(1))
                .extract().jsonPath().getLong("id");

        // Step 4: 사용자 C가 대기 (waitingOrder: 2)
        Map<String, Object> reservationC = Map.of(
                "name", "사용자C",
                "date", date,
                "timeId", timeId,
                "themeId", themeId
        );
        long idC = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationC)
                .when().post("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("waitingOrder", is(2))
                .extract().jsonPath().getLong("id");

        // Step 5: 사용자 B 조회 시 본인의 대기 순번(1) 확인
        RestAssured.given().log().all()
                .param("name", "사용자B")
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].waitingOrder", is(1));

        // Step 6: 사용자 A(예약자)가 예약 취소
        RestAssured.given().log().all()
                .param("name", "사용자A")
                .when().delete("/user/reservations/" + idA)
                .then().log().all()
                .statusCode(204);

        // Step 7: 순번 앞당겨짐 확인
        // 사용자 B: 1 -> 0
        RestAssured.given().log().all()
                .param("name", "사용자B")
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].waitingOrder", is(0));

        // 사용자 C: 2 -> 1
        RestAssured.given().log().all()
                .param("name", "사용자C")
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("[0].waitingOrder", is(1));

        // Step 8: 사용자 C(대기자)가 대기 취소
        RestAssured.given().log().all()
                .param("name", "사용자C")
                .when().delete("/user/reservations/" + idC)
                .then().log().all()
                .statusCode(204);

        // 최종 확인: 사용자 C 예약 목록 비어있음
        RestAssured.given().log().all()
                .param("name", "사용자C")
                .when().get("/user/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }
}
