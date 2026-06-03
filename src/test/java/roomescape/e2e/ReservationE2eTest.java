package roomescape.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.support.ApiFixtures.예약_생성;
import static roomescape.support.ApiFixtures.시간_등록;
import static roomescape.support.ApiFixtures.테마_등록;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.theme.dto.response.ThemeResponseDto;
import roomescape.feature.time.dto.response.TimeResponseDto;
import roomescape.support.DatabaseCleaner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationE2eTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

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
    void 시간과_테마를_등록한_뒤_예약을_생성하고_조회한다() {
        // given
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마", "설명", "https://example.com/image.png");

        // when
        예약_생성("브라운", FUTURE_DATE, time.id(), theme.id());

        // then
        List<ReservationResponseDto> myReservations = given()
            .queryParam("name", "브라운")
            .when().get("/api/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(myReservations).hasSize(1);
        assertThat(myReservations.getFirst().name()).isEqualTo("브라운");
        assertThat(myReservations.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);

        List<ReservationResponseDto> adminReservations = given()
            .when().get("/api/admin/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(adminReservations).hasSize(1);
    }

    @Test
    void 예약된_슬롯에_대기를_생성하고_취소한다() {
        // given
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마", "설명", "https://example.com/image.png");
        예약_생성("브라운", FUTURE_DATE, time.id(), theme.id());

        ReservationCreateResponseDto waiting = given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("리사", FUTURE_DATE, time.id(), theme.id()))
            .when().post("/api/reservations/waitings")
            .then().statusCode(201)
            .extract().as(ReservationCreateResponseDto.class);

        // when
        given()
            .queryParam("name", "리사")
            .when().patch("/api/reservations/waitings/{id}/cancel", waiting.id())
            .then().statusCode(200);

        // then
        List<ReservationResponseDto> reservations = given()
            .queryParam("name", "리사")
            .when().get("/api/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().status()).isEqualTo(ReservationEditableStatus.CANCELED);
    }

    @Test
    void 예약을_취소하면_가장_빠른_순번의_대기가_예약으로_자동_승격된다() {
        // given
        TimeResponseDto time = 시간_등록(LocalTime.of(10, 0));
        ThemeResponseDto theme = 테마_등록("테마", "설명", "https://example.com/image.png");

        String activeReservationName = "브라운";
        ReservationCreateResponseDto active = 예약_생성(activeReservationName, FUTURE_DATE, time.id(), theme.id());

        String firstWaitingName = "리사";
        String secondWaitingName = "네오";
        ReservationCreateResponseDto firstWaiting = 대기_생성(firstWaitingName, FUTURE_DATE, time.id(), theme.id());
        대기_생성(secondWaitingName, FUTURE_DATE, time.id(), theme.id());

        // when
        given()
            .queryParam("name", activeReservationName)
            .when().patch("/api/reservations/{id}/cancel", active.id())
            .then().statusCode(200);

        // then
        List<ReservationResponseDto> firstWaitingAfterCancel = given()
            .queryParam("name", firstWaitingName)
            .when().get("/api/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(firstWaitingAfterCancel).hasSize(1);
        assertThat(firstWaitingAfterCancel.getFirst().id()).isEqualTo(firstWaiting.id());
        assertThat(firstWaitingAfterCancel.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);

        List<ReservationResponseDto> secondWaitingAfterCancel = given()
            .queryParam("name", secondWaitingName)
            .when().get("/api/reservations")
            .then().statusCode(200)
            .extract().as(new TypeRef<>() {
            });
        assertThat(secondWaitingAfterCancel).hasSize(1);
        assertThat(secondWaitingAfterCancel.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING);
        assertThat(secondWaitingAfterCancel.getFirst().waitingNumber()).isEqualTo(1);
    }

    private ReservationCreateResponseDto 대기_생성(String name, LocalDate date, Long timeId, Long themeId) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto(name, date, timeId, themeId))
            .when().post("/api/reservations/waitings")
            .then().statusCode(201)
            .extract().as(ReservationCreateResponseDto.class);
    }
}
