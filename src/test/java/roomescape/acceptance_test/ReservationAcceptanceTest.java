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
import roomescape.controller.dto.ReservationCreateRequest;
import roomescape.controller.dto.ReservationTimeCreateRequest;
import roomescape.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("예약 생성 후 관리자 페이지에서 예약 목록을 조회한다.")
    public void scenario1() throws JsonProcessingException {
        LocalTime startAt = LocalTime.of(10, 30);
        ReservationTimeCreateRequest timeRequest = new ReservationTimeCreateRequest(startAt);
        Integer reservationTimeId = createReservationTime(timeRequest);

        ThemeCreateRequest themeRequest = new ThemeCreateRequest("테마1", "설명", "섬네일");
        Integer themeId = createTheme(themeRequest);

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());

        Integer reservationId = createReservation(reservationRequest, reservationTimeId, themeId);

        given().log().all()
        .when()
            .get("/admin/reservations")
        .then().log().all()
            .statusCode(200)
            .body("reservations.id", hasItem(reservationId))
            .body("reservations.name", hasItem(reservationRequest.name()))
            .body("reservations.date", hasItem(reservationRequest.date().toString()))
            .body("reservations.time.id", hasItem(reservationTimeId))
            .body("reservations.theme.id", hasItem(themeId));
    }

    @Test
    @DisplayName("예약 삭제 후 관리자 예약 목록에서 사라진다.")
    public void scenario2() throws JsonProcessingException {
        LocalTime startAt = LocalTime.of(10, 30);
        ReservationTimeCreateRequest timeRequest = new ReservationTimeCreateRequest(startAt);
        Integer reservationTimeId = createReservationTime(timeRequest);

        ThemeCreateRequest themeRequest = new ThemeCreateRequest("테마1", "설명", "섬네일");
        Integer themeId = createTheme(themeRequest);

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());

        Integer reservationId = createReservation(reservationRequest, reservationTimeId, themeId);

        given().log().all()
                .pathParam("id", reservationId)
        .when()
                .delete("/admin/reservations/{id}")
        .then().log().all()
                .statusCode(204);

        given().log().all()
        .when()
                .get("/admin/reservations")
        .then().log().all()
                .statusCode(200)
                .body("reservations.id", not(hasItem(reservationId)));
    }

    private Integer createReservationTime(ReservationTimeCreateRequest request) throws JsonProcessingException {
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
                .body("id", notNullValue())
                .body("name", equalTo(request.name()))
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(reservationTimeId))
                .body("theme.id", equalTo(themeId))
                .extract().path("id");
    }

}
