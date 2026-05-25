package roomescape.acceptance_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.controller.dto.ReservationCreateRequest;
import roomescape.reservation.controller.dto.ReservationEditRequest;
import roomescape.reservationtime.controller.dto.ReservationTimeCreateRequest;
import roomescape.theme.controller.dto.ThemeCreateRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static roomescape.common.auth.UserArgumentResolver.GUEST_NAME_HEADER;


public class ReservationAcceptanceTest extends AcceptanceTestSupport {

    @Test
    @DisplayName("예약 생성 후 관리자 페이지에서 예약 목록을 조회한다.")
    public void scenario1() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        ReservationCreateRequest reservationRequest = createScenario1Fixture();
        Integer reservationId = createReservation(reservationRequest);

        given().log().all()
                .when()
                .get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.id", hasItem(reservationId))
                .body("reservations.guestName", hasItem(reservationRequest.guestName()))
                .body("reservations.date", hasItem(reservationRequest.date().toString()))
                .body("reservations.time.id", hasItem(reservationRequest.timeId().intValue()))
                .body("reservations.theme.id", hasItem(reservationRequest.themeId().intValue()));
    }

    private ReservationCreateRequest createScenario1Fixture() throws JsonProcessingException {
        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));
        return new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());
    }

    @Test
    @DisplayName("예약 삭제 후 관리자 예약 목록에서 사라진다.")
    public void scenario2() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        Integer reservationId = createScenario2Fixture();

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

    private Integer createScenario2Fixture() throws JsonProcessingException {
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

        return createReservation(reservationRequest);
    }

    @Test
    @DisplayName("특정 사용자의 이름을 입력해 예약을 조회한다.")
    public void scenario3() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        String guestName = "brown";
        createScenario3Fixture(guestName);

        given().log().all()
                .header(GUEST_NAME_HEADER, guestName)
                .when()
                .get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("reservations.guestName", hasItem(guestName));
    }

    private void createScenario3Fixture(String guestName) throws JsonProcessingException {
        LocalTime startAt = LocalTime.of(10, 30);
        ReservationTimeCreateRequest timeRequest = new ReservationTimeCreateRequest(startAt);
        Integer reservationTimeId = createReservationTime(timeRequest);

        ThemeCreateRequest themeRequest = new ThemeCreateRequest("테마1", "설명", "섬네일");
        Integer themeId = createTheme(themeRequest);

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                guestName,
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());

        createReservation(reservationRequest);
    }

    @Test
    @DisplayName("예약의 날짜와 시간을 수정한다.")
    public void scenario4() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        LocalDate originalDate = LocalDate.of(2026, 10, 14);
        LocalDate editedDate = LocalDate.of(2026, 10, 15);

        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                originalDate,
                reservationTimeId.longValue(),
                themeId.longValue());
        Integer reservationId = createReservation(reservationRequest);

        ReservationEditRequest editRequest = new ReservationEditRequest(
                editedDate,
                editedReservationTimeId.longValue());

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(editRequest))
                .pathParam("id", reservationId)
                .header(GUEST_NAME_HEADER, reservationRequest.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .statusCode(200)
                .body("id", equalTo(reservationId))
                .body("guestName", equalTo(reservationRequest.guestName()))
                .body("date", equalTo(editRequest.date().toString()))
                .body("time.id", equalTo(editedReservationTimeId))
                .body("theme.id", equalTo(themeId));
    }

    @Test
    @DisplayName("수정하려는 날짜와 시간에 같은 테마의 예약이 존재하면 에러가 발생한다.")
    public void scenario5() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());
        createReservation(reservationRequest);

        ReservationCreateRequest targetReservationRequest = new ReservationCreateRequest(
                "pobi",
                LocalDate.of(2026, 10, 15),
                editedReservationTimeId.longValue(),
                themeId.longValue());
        Integer targetReservationId = createReservation(targetReservationRequest);

        ReservationEditRequest editRequest = new ReservationEditRequest(
                reservationRequest.date(),
                reservationRequest.timeId());

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(editRequest))
                .pathParam("id", targetReservationId)
                .header(GUEST_NAME_HEADER, targetReservationRequest.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("이미 시작된 예약은 수정할 수 없다.")
    public void scenario6() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        LocalDate reservationDate = LocalDate.of(2026, 10, 14);

        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                reservationDate,
                reservationTimeId.longValue(),
                themeId.longValue());
        Integer reservationId = createReservation(reservationRequest);

        mutableClock.setFixed(LocalDateTime.of(2026, 10, 14, 10, 31));

        ReservationEditRequest editRequest = new ReservationEditRequest(
                LocalDate.of(2026, 10, 15),
                editedReservationTimeId.longValue());

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(editRequest))
                .pathParam("id", reservationId)
                .header(GUEST_NAME_HEADER, reservationRequest.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("이미 지난 날짜와 시간으로 예약을 수정할 수 없다.")
    public void scenario7() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());
        Integer reservationId = createReservation(reservationRequest);

        mutableClock.setFixed(LocalDateTime.of(2026, 10, 10, 12, 0));

        ReservationEditRequest editRequest = new ReservationEditRequest(
                LocalDate.of(2026, 10, 10),
                editedReservationTimeId.longValue());

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(editRequest))
                .pathParam("id", reservationId)
                .header(GUEST_NAME_HEADER, reservationRequest.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .statusCode(422);
    }

    @Test
    @DisplayName("본인의 예약이 아니면 수정할 수 없다.")
    public void scenario8() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer editedReservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(11, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest otherReservation = new ReservationCreateRequest(
                "brown",
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());
        createReservation(otherReservation);

        ReservationCreateRequest myReservation = new ReservationCreateRequest(
                "pobi",
                LocalDate.of(2026, 10, 15),
                editedReservationTimeId.longValue(),
                themeId.longValue());
        Integer myReservationId = createReservation(myReservation);

        ReservationEditRequest editRequest = new ReservationEditRequest(
                otherReservation.date(),
                otherReservation.timeId());

        given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(editRequest))
                .pathParam("id", myReservationId)
                .header(GUEST_NAME_HEADER, otherReservation.guestName())
                .when()
                .patch("/reservations/{id}")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("본인의 예약을 삭제한다.")
    public void scenario9() throws JsonProcessingException {
        mutableClock.setFixed(LocalDate.of(2026, 5, 12));
        String guestName = "brown";
        Integer reservationTimeId = createReservationTime(
                new ReservationTimeCreateRequest(LocalTime.of(10, 30)));
        Integer themeId = createTheme(
                new ThemeCreateRequest("테마1", "설명", "섬네일"));

        ReservationCreateRequest reservationRequest = new ReservationCreateRequest(
                guestName,
                LocalDate.of(2026, 10, 14),
                reservationTimeId.longValue(),
                themeId.longValue());
        Integer reservationId = createReservation(reservationRequest);

        given().log().all()
                .pathParam("id", reservationId)
                .header(GUEST_NAME_HEADER, guestName)
                .when()
                .delete("/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        given().log().all()
                .header(GUEST_NAME_HEADER, guestName)
                .when()
                .get("/reservations/me")
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
            ReservationCreateRequest request
    ) throws JsonProcessingException {
        return given().log().all()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(request))
                .when()
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("guestName", equalTo(request.guestName()))
                .body("date", equalTo(request.date().toString()))
                .body("time.id", equalTo(request.timeId().intValue()))
                .body("theme.id", equalTo(request.themeId().intValue()))
                .extract().path("id");
    }

}
