package roomescape.controller.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static roomescape.TokenTestFixture.ADMIN_TOKEN;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.controller.dto.CreateTimeRequest;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class AdminReservationTimeControllerTest {

    @DisplayName("성공: 예약 시간 저장 -> 201")
    @Test
    void save() {
        CreateTimeRequest request = new CreateTimeRequest("00:00");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(201)
            .body("id", is(3))
            .body("startAt", is("00:00"));
    }

    @DisplayName("성공: 예약 시간 삭제 -> 204")
    @Test
    void delete() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .when().delete("/admin/times/2")
            .then().log().all()
            .statusCode(204);

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1));
    }

    @DisplayName("성공: 예약 시간 조회 -> 200")
    @Test
    void findAll() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .when().get("/admin/times")
            .then().log().all()
            .statusCode(200)
            .body("id", contains(1, 2))
            .body("startAt", contains("10:00", "23:00"));
    }

    @DisplayName("실패: 잘못된 포맷의 예약 시간 저장 -> 400")
    @Test
    void save_IllegalTimeFormat() {
        CreateTimeRequest request = new CreateTimeRequest("24:00");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(400)
            .body("message", is("잘못된 시간 형식입니다."));
    }

    @DisplayName("예약이 존재하는 시간 삭제 -> 400")
    @Test
    void delete_ReservationExists() {
        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .when().delete("/admin/times/1")
            .then().log().all()
            .statusCode(400)
            .body("message", is("해당 시간을 사용하는 예약이 존재하여 삭제할 수 없습니다."));
    }

    @DisplayName("실패: 이미 존재하는 시간을 저장 -> 400")
    @Test
    void save_Duplicate() {
        CreateTimeRequest request = new CreateTimeRequest("10:00");

        RestAssured.given().log().all()
            .cookie("token", ADMIN_TOKEN)
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/admin/times")
            .then().log().all()
            .statusCode(400)
            .body("message", is("이미 존재하는 시간은 추가할 수 없습니다."));
    }
}

