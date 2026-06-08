package roomescape.controller;

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
public class ReservationControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0))).getId();
        themeId = themeRepository.save(new Theme(null, "테마", "설명", "/url")).getId();
    }

    @Test
    void 예약과_시간_연결() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations")
                .then().log().all().statusCode(201);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all().statusCode(200)
                .body("size()", is(1));
    }

    @Test
    void 이름_기반_예약_조회_API() {
        RestAssured.given().log().all().queryParam("name", "아나키")
                .when().get("/reservations/my-reservation")
                .then().log().all().statusCode(200);
    }

    @Test
    void 예약_삭제_API() {
        ReservationTime time = reservationTimeRepository.findTimeById(timeId);
        Theme theme = themeRepository.findThemeById(themeId);
        Reservation saved = reservationRepository.save(
                new Reservation("브라운", LocalDate.now().plusDays(1), time, theme, LocalDateTime.now()));

        RestAssured.given().log().all()
                .when().delete("/reservations/" + saved.getId())
                .then().log().all().statusCode(204);
    }
}
