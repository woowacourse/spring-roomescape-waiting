package roomescape.time.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.time.controller.dto.ReservationTimeRequest;

@SpringWebTest
public class ReservationTimeAdminControllerIntegrationTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @Test
    @DisplayName("관리자가 예약 시간을 생성한다.")
    void createTime_success() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", is("10:00:00"));
    }

    @Test
    @DisplayName("관리자가 예약 시간을 삭제한다.")
    void deleteTime_success() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));

        Long id = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/admin/times/" + id)
                .then().log().all()
                .statusCode(204);
    }
}
