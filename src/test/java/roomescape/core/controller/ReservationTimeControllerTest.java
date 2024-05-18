package roomescape.core.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.core.utils.e2eTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReservationTimeControllerTest {
    private static final String TOMORROW_DATE = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("전체 시간 목록을 조회한다.")
    void findAll() {
        ValidatableResponse response = e2eTest.get("/times");
        response.statusCode(200)
                .body("size()", is(3));
    }

    @Test
    @DisplayName("날짜와 테마 정보가 주어지면 예약 가능한 시간 목록을 조회한다.")
    void findBookable() {
        ValidatableResponse response = e2eTest.get("/times?date=" + TOMORROW_DATE + "&theme=1");
        response.statusCode(200)
                .body("size()", is(3));
    }
}
