package roomescape.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.presentation.dto.PlayTimeRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("새로운 예약 시간을 생성하면 201 상태코드와 함께 생성된 시간이 반환된다")
    void create_ValidTime_ReturnsCreatedTime() {
        // given
        final PlayTimeRequest request = new PlayTimeRequest(LocalTime.of(14, 0));

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/times")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("startAt", equalTo("14:00:00"));
    }

    @Test
    @DisplayName("이미 존재하는 예약 시간을 생성하려고 하면 409 상태코드를 반환한다")
    void create_DuplicateTime_ReturnsConflict() {
        // given
        final PlayTimeRequest request = new PlayTimeRequest(LocalTime.of(14, 0));

        // 첫 번째 생성
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/times");

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/times")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("잘못된 형식의 시간으로 생성 요청하면 400 상태코드를 반환한다")
    void create_InvalidTimeFormat_ReturnsBadRequest() {
        // given
        final String invalidRequest = "{\"time\": \"invalid-time\"}";

        // when & then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidRequest)
                .when()
                .post("/times")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("모든 예약 시간을 조회하면 200 상태코드와 함께 예약 시간 목록이 반환된다")
    void readAll_ReturnsAllTimes() {
        // given
        final PlayTimeRequest time1 = new PlayTimeRequest(LocalTime.of(14, 0));
        final PlayTimeRequest time2 = new PlayTimeRequest(LocalTime.of(16, 0));

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(time1)
                .post("/times");

        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(time2)
                .post("/times");

        // when & then
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(2))
                .body("startAt", hasItems("14:00:00", "16:00:00"));
    }

    @Test
    @DisplayName("예약 시간이 없을 때 조회하면 200 상태코드와 함께 빈 목록이 반환된다")
    void readAll_WhenNoTimes_ReturnsEmptyList() {
        given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("", hasSize(0));
    }

    @Test
    @DisplayName("존재하는 예약 시간을 삭제하면 204 상태코드를 반환한다")
    void delete_ExistingTime_ReturnsNoContent() {
        // given
        final PlayTimeRequest request = new PlayTimeRequest(LocalTime.of(14, 0));

        final Long timeId = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .post("/times")
                .then()
                .extract()
                .jsonPath()
                .getLong("id");

        // when & then
        given()
                .when()
                .delete("/times/{id}", timeId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하려고 하면 404 상태코드를 반환한다")
    void delete_NonExistingTime_ReturnsNotFound() {
        given()
                .when()
                .delete("/times/{id}", 999L)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
