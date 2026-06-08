package roomescape.controller.admin;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminReservationTimeControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    void 시간_관리_API() {
        Map<String, Object> request = new HashMap<>();
        request.put("startAt", "22:00");

        long createdId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + createdId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_시간_삭제_불가() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(new Theme(null, "테마", "설명", "/url"));
        reservationRepository.save(
                new Reservation("브라운", LocalDate.now().plusDays(1), time, theme, LocalDateTime.now()));

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + time.getId())
                .then().log().all()
                .statusCode(400)
                .body("message", equalTo("해당 시간에 예약이 존재하여 삭제할 수 없습니다."));
    }
}
