package roomescape.time.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.testSupport.RestAssuredTestHelper.createTheme;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.time.exception.TimeErrorCode;

@SpringWebTest
public class ReservationTimeAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("관리자가 예약 시간을 성공적으로 생성한다.")
    void createTime_Success() throws Exception {
        Map<String, Object> request = Map.of("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", is("/times/1"))
                .body("id", is(1))
                .body("startAt", is("10:00:00"));
    }

    @Test
    @DisplayName("시간 생성 시 startAt이 누락되면 400 에러를 반환한다.")
    void createTime_MissingStartAt_BadRequest() throws Exception {
        Map<String, Object> request = new HashMap<>();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(400)
                .body("message", is("시간 형식이 올바르지 않습니다. 'HH:mm' 포맷에 맞춰 다시 입력하십시오."));
    }

    @Test
    @DisplayName("중복된 예약 시간 생성 시 409 에러를 반환한다.")
    void createTime_Duplicate_Conflict() throws Exception {
        Map<String, Object> request = Map.of("startAt", "10:00");

        // First creation
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().statusCode(201);

        // Duplicate creation
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(409)
                .body("message", is(TimeErrorCode.DUPLICATE_TIME.getMessage()));
    }

    @Test
    @DisplayName("관리자가 예약 시간을 성공적으로 삭제한다.")
    void delete_Success() throws Exception {
        Map<String, Object> request = Map.of("startAt", "10:00");

        Long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/" + id)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 삭제 시 404 에러를 반환한다.")
    void delete_NotFound_NotFound() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/9999")
                .then().log().all()
                .statusCode(404)
                .body("message", is(TimeErrorCode.TIME_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("사용 중인 예약 시간 삭제 시 409 에러를 반환한다.")
    void delete_InUse_Conflict() throws Exception {
        // given
        createTheme("우아한 테마", "설명", "https://example.com/woowa.png");

        Map<String, Object> request = Map.of("startAt", "10:00");
        Long id = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // timeId = id를 참조하는 예약을 직접 삽입하여 사용 중인 상태로 만듦
        databaseHelper.insertReservationDirectly("브라운", LocalDate.now().plusDays(1), id, 1L);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/" + id)
                .then().log().all()
                .statusCode(409)
                .body("message", is(TimeErrorCode.TIME_IN_USE.getMessage()));
    }
}
