package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationTimeAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예약 시간 생성 후 목록에서 조회된다")
    public void scenario1() throws JsonProcessingException {
        LocalTime startAt = LocalTime.of(10, 30);
        int reservationTimeId = createReservationTime(new ReservationTimeCreateRequest(startAt));

        given()
                .log().all()
        .when()
                .get("/times")
        .then()
                .log().all()
                .statusCode(200)
                .body("times.id", hasItem(reservationTimeId))
                .body("times.startAt", hasItem(startAt.toString()));
    }

    @Test
    @DisplayName("중복된 예약 시간을 생성하면 에러가 발생한다")
    public void scenario2() throws JsonProcessingException {
        LocalTime startAt = LocalTime.of(10, 30);
        ReservationTimeCreateRequest request = new ReservationTimeCreateRequest(startAt);

        createReservationTime(request); // 1차 생성

        // 중복된 시간 생성 시도 -> 400
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
        .when()
                .post("/admin/times")
        .then()
                .log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("특정 날짜와 테마의 예약 가능한 시간을 조회한다")
    public void scenario3() throws JsonProcessingException {
        LocalDate date = LocalDate.of(2026, 10, 14);
        ReservationTimeCreateRequest timeRequest = new ReservationTimeCreateRequest(LocalTime.of(21, 30));
        ReservationTimeCreateRequest timeRequest2 = new ReservationTimeCreateRequest(LocalTime.of(22, 30));
        Integer reservationTimeId = createReservationTime(timeRequest);
        Integer reservationTimeId2 = createReservationTime(timeRequest2);

        ThemeCreateRequest themeRequest = new ThemeCreateRequest("테마1", "설명", "섬네일");
        Integer themeId = createTheme(themeRequest);

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                date,
                reservationTimeId.longValue(),
                themeId.longValue());
        createReservation(reservationRequest, reservationTimeId, themeId);

        given()
                .log().all()
                .queryParam("date", date.toString())
                .queryParam("themeId", themeId)
        .when()
                .get("/times/availability")
        .then()
                .log().all()
                .statusCode(200)
                .body("availableTimes.find { it.id == " + reservationTimeId + " }.isAvailable", equalTo(false))
                .body("availableTimes.find { it.id == " + reservationTimeId2 + " }.isAvailable", equalTo(true));
    }

    private int createReservationTime(ReservationTimeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .body("startAt", equalTo(request.startAt().toString()))
                .extract().path("id");
    }

    private Integer createTheme(ThemeCreateRequest request) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .body("name", equalTo(request.name()))
                .body("description", equalTo(request.description()))
                .body("thumbnail", equalTo(request.thumbnail()))
                .extract().path("id");
    }

    private Integer createReservation(
            ReservationCreateRequest request,
            Integer reservationTimeId,
            Integer themeId
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("guestName", equalTo(request.guestName()))
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(reservationTimeId))
                .body("theme.id", equalTo(themeId))
                .extract().path("id");
    }
}
