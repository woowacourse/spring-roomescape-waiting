package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.time.ReservationTimeCreateRequestDto;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerTest {

    @DisplayName("목록 내용 갯수를 검사한다")
    @Test
    void timesTest() {
        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Nested
    @DisplayName("예약시간 생성")
    class ReservationTimePostTest {

        @DisplayName("Time 입력 테스트")
        @Test
        void addReservationTimeTest() {
            LocalTime reservationTime = LocalTime.of(15, 30);
            ReservationTimeCreateRequestDto requestTime = new ReservationTimeCreateRequestDto(reservationTime);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("id", is(1));

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1));
        }

        @DisplayName("times 응답의 LocalTime 형식은 xx:xx 이다.")
        @Test
        void timeResponseTest() {
            LocalTime reservationTime = LocalTime.of(15, 40);
            ReservationTimeCreateRequestDto requestTime = new ReservationTimeCreateRequestDto(reservationTime);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .body("[0].startAt", equalTo("15:40"));
        }

        @DisplayName("올바른 시간의 포멧만 요청 가능하다.")
        @Test
        void invalidRequestTimeTest1() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "15:40:00");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(500);
        }

        @DisplayName("유효한 시간만 생성 가능하다")
        @Test
        void invalidRequestTimeTest2() {
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "25:40");

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(500);
        }
    }

    @Nested
    @DisplayName("예약시간 삭제")
    class DeleteReservationTimeTest {

        @DisplayName("저장된 Id 제거 테스트")
        @Test
        void deleteTimeTest() {
            LocalTime reservationTime = LocalTime.of(15, 30);
            ReservationTimeCreateRequestDto requestTime = new ReservationTimeCreateRequestDto(reservationTime);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(requestTime)
                    .when().post("/times")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().delete("/times/1")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(0));
        }

        @DisplayName("존재하지 않는 Id의 Time 을 삭제할 수 없다")
        @Test
        void invalidTimeIdTest() {
            RestAssured.given().log().all()
                    .when().delete("/times/5")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
