package roomescape.feature.reservation.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static roomescape.support.ApiFixtures.시간_등록;
import static roomescape.support.ApiFixtures.테마_등록;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.support.DatabaseCleaner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationAvailabilityFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 예약을_생성하면_해당_슬롯의_available이_false가_된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마1", "설명1", "http://image1.png");

        // when
        List<TimeAvailabilityResponseDto> beforeReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.id())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.id(), theme.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .body("name", equalTo("예약자"))
            .body("date", equalTo(date.toString()))
            .body("timeId", equalTo(time.id().intValue()))
            .body("themeId", equalTo(theme.id().intValue()));

        List<TimeAvailabilityResponseDto> afterReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.id())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean beforeAllAvailable = beforeReservationTimes.stream().allMatch(TimeAvailabilityResponseDto::available);
        assertThat(beforeAllAvailable).isTrue();

        boolean afterNotAvailable = afterReservationTimes.stream()
            .filter(timeAvailability -> Objects.equals(timeAvailability.id(), time.id()))
            .noneMatch(TimeAvailabilityResponseDto::available);

        boolean afterTimeContains = afterReservationTimes.stream()
            .anyMatch(timeAvailability -> Objects.equals(timeAvailability.id(), time.id()));

        assertThat(afterNotAvailable).isTrue();
        assertThat(afterTimeContains).isTrue();
    }

    @Test
    void 생성한_예약을_조회한다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마1", "설명1", "http://image1.png");

        // when
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.id(), theme.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201);

        List<ReservationResponseDto> reservations = given()
            .queryParam("name", "예약자")
            .when().get("/api/reservations")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().name()).isEqualTo("예약자");
        assertThat(reservations.getFirst().date()).isEqualTo(date);
        assertThat(reservations.getFirst().time().id()).isEqualTo(time.id());
        assertThat(reservations.getFirst().theme().id()).isEqualTo(theme.id());
        assertThat(reservations.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);
    }

    @Test
    void 예약이_존재하는_슬롯에_대기를_생성하고_조회한다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마1", "설명1", "http://image1.png");

        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.id(), theme.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201);

        // when
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("대기자", date, time.id(), theme.id()))
            .when().post("/api/reservations/waitings")
            .then()
            .statusCode(201);

        List<ReservationResponseDto> waitings = given()
            .queryParam("name", "대기자")
            .when().get("/api/reservations")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().name()).isEqualTo("대기자");
        assertThat(waitings.getFirst().date()).isEqualTo(date);
        assertThat(waitings.getFirst().time().id()).isEqualTo(time.id());
        assertThat(waitings.getFirst().theme().id()).isEqualTo(theme.id());
        assertThat(waitings.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING);
        assertThat(waitings.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void 예약을_취소하면_해당_슬롯의_available이_다시_true가_된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마1", "설명1", "http://image1.png");

        ReservationCreateResponseDto reservation = given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.id(), theme.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .extract()
            .as(ReservationCreateResponseDto.class);

        // when
        given()
            .queryParam("name", "예약자")
            .when().patch("/api/reservations/{id}/cancel", reservation.id())
            .then()
            .statusCode(200);

        List<TimeAvailabilityResponseDto> timesAfterCancel = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.id())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean isAvailableAgain = timesAfterCancel.stream()
            .filter(t -> Objects.equals(t.id(), time.id()))
            .allMatch(TimeAvailabilityResponseDto::available);

        assertThat(isAvailableAgain).isTrue();
    }

    @Test
    void 예약을_수정하면_기존_슬롯은_available이_되고_새_슬롯은_available이_아니게_된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto timeA = 시간_등록(LocalTime.of(10, 0));
        TimeResponseDto timeB = 시간_등록(LocalTime.of(11, 0));
        ThemeResponseDto theme = 테마_등록("테마1", "설명1", "http://image1.png");

        ReservationCreateResponseDto reservation = given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, timeA.id(), theme.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .extract()
            .as(ReservationCreateResponseDto.class);

        // when
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationUpdateRequestDto("예약자", date, timeB.id(), theme.id()))
            .when().patch("/api/reservations/{id}", reservation.id())
            .then()
            .statusCode(200);

        List<TimeAvailabilityResponseDto> timesAfterUpdate = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", theme.id())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean timeAIsAvailable = timesAfterUpdate.stream()
            .filter(t -> Objects.equals(t.id(), timeA.id()))
            .allMatch(TimeAvailabilityResponseDto::available);

        boolean timeBIsNotAvailable = timesAfterUpdate.stream()
            .filter(t -> Objects.equals(t.id(), timeB.id()))
            .noneMatch(TimeAvailabilityResponseDto::available);

        assertThat(timeAIsAvailable).isTrue();
        assertThat(timeBIsNotAvailable).isTrue();
    }

    @Test
    void 한_테마에_예약을_생성해도_다른_테마_같은_슬롯의_available은_변하지_않는다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto themeA = 테마_등록("테마A", "설명A", "http://imageA.png");
        ThemeResponseDto themeB = 테마_등록("테마B", "설명B", "http://imageB.png");

        // when
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.id(), themeA.id()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201);

        List<TimeAvailabilityResponseDto> themeBTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", themeB.id())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean isAvailable = themeBTimes.stream()
            .filter(t -> Objects.equals(t.id(), time.id()))
            .allMatch(TimeAvailabilityResponseDto::available);

        assertThat(isAvailable).isTrue();
    }
}
