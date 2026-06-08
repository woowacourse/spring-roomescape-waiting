package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationTimeControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }

    @Test
    void 전체_시간_조회_API() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
