package roomescape.e2e;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.e2e.support.DatabaseHelper;
import roomescape.e2e.support.SpringWebTest;

@SpringWebTest
public class ReservationE2ETest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @DisplayName("예약 시간과 테마를 생성한 뒤 예약을 생성하고 목록에서 조회한다.")
    @Test
    void createReservation() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "brown");
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        Map<String, Object> theme = new HashMap<>();
        theme.put("name", "우아한 테마");
        theme.put("description", "우아한테크코스 전용 테마입니다.");
        theme.put("thumbnailUrl", "https://example.com/image.png");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"startAt\": \"10:00\"}")
                .when().post("/admin/times")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }
}
