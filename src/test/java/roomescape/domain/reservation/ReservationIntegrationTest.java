package roomescape.domain.reservation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.TestFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReservationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestFixture testFixture;

    @MockitoSpyBean
    private JpaReservationRepository reservationRepository;

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            ZoneId zoneId = ZoneId.systemDefault();
            return Clock.fixed(
                LocalDateTime.of(2026, 5, 31, 13, 0)
                    .atZone(zoneId)
                    .toInstant(),
                zoneId
            );
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        testFixture.clear();
    }

    @Test
    @DisplayName("예약 생성을 end-to-end로 확인한다.")
    void createReservation() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "timeId": %d,
                "themeId": %d
            }
            """.formatted(dateId, timeId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(201)
            .body("date", is("2026-06-01"))
            .body("time", is("10:00"))
            .body("theme.name", is("공포"))
            .body("theme.content", is("무서운 테마"))
            .body("theme.url", is("theme-url"));

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations", hasSize(1));
    }

    @Test
    @DisplayName("예약 생성 시 시간 필드가 누락되었을 경우 400 에러가 발생한다.")
    void createReservationWithoutTimeId() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");

        String request = """
            {
                "name": "보예",
                "dateId": %d,
                "themeId": %d
            }
            """.formatted(dateId, themeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("INPUT_VALIDATION_ERROR"))
            .body("message", is("시간은 필수 선택 사항 입니다. 시간을 선택해주세요."));
    }

    @Test
    @DisplayName("예약자 이름으로 예약 조회를 end-to-end로 확인한다.")
    void getUserReservations() {
        saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations[0].reservationSlot.date.startWhen", is("2026-06-01"))
            .body("reservations[0].reservationSlot.time.startAt", is("10:00"))
            .body("reservations[0].reservationSlot.theme.name", is("공포"))
            .body("reservations[0].status", is("CONFIRMED"));
    }

    @Test
    @DisplayName("사용자 예약 조회 시 같은 슬롯의 대기 순서를 기준으로 대기번호를 계산한다.")
    void getUserReservationsWithWaitingNumberInSameSlot() {
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        Long reservationSlotId = saveReservationSlot(dateId, timeId, themeId);
        saveReservation("보예", reservationSlotId, ReservationStatus.CONFIRMED);
        saveReservation("수민", reservationSlotId, ReservationStatus.WAITING);
        saveReservation("말랑", reservationSlotId, ReservationStatus.WAITING);

        given().log().all()
            .contentType(ContentType.JSON)
            .param("name", "말랑")
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200)
            .body("username", is("말랑"))
            .body("reservations[0].status", is("WAITING"))
            .body("reservations[0].waitingNumber", is(2));
    }

    @Test
    @DisplayName("예약 조회 시 이름 파라미터가 누락되었을 경우 400 에러가 발생한다.")
    void getUserReservationsWithoutName() {
        given().log().all()
            .contentType(ContentType.JSON)
            .when().get("/reservations")
            .then().log().all()
            .statusCode(400)
            .body("code", is("REQUIRED_PARAMETER_MISSING"))
            .body("message", is("필수 요청 파라미터가 누락되었습니다."));
    }

    @Test
    @DisplayName("예약 삭제를 end-to-end로 확인한다.")
    void deleteUserReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");

        given().log().all()
            .contentType(ContentType.JSON)
            .when().delete("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("username", is("보예"))
            .body("reservations", hasSize(1))
            .body("reservations[0].status", is("CANCELED"))
            .body("reservations[0].waitingNumber", nullValue());
    }

    @Test
    @DisplayName("예약 취소 후 대기 전환 중 예외가 발생하면 예약 취소도 롤백된다.")
    void checkRollback() {
        // given
        Long themeId = saveTheme("공포");
        Long dateId = saveDate("2026-06-01");
        Long timeId = saveTime("10:00");
        Long reservationSlotId = saveReservationSlot(dateId, timeId, themeId);
        Long confirmedReservationId = saveReservation(
            "보예",
            reservationSlotId,
            ReservationStatus.CONFIRMED
        );
        Long waitingReservationId = saveReservation(
            "수민",
            reservationSlotId,
            ReservationStatus.WAITING
        );
        doAnswer(invocationOnMock -> {
                Reservation reservation = invocationOnMock.getArgument(0);
                if (reservation.getId().equals(waitingReservationId)) {
                    throw new IllegalArgumentException("대기 전환에 실패했습니다.");
                }
                return invocationOnMock.callRealMethod();
            }
        ).when(reservationRepository).save(any(Reservation.class));

        // when & then
        given().
            contentType(ContentType.JSON)
            .when().delete("/reservations/{id}", confirmedReservationId)
            .then()
            .statusCode(500);

        assertSoftly(softly -> {
                assertThat(findReservationStatus(confirmedReservationId)).isEqualTo(ReservationStatus.CONFIRMED);
                assertThat(findReservationStatus(waitingReservationId)).isEqualTo(ReservationStatus.WAITING);
            }
        );
    }

    @Test
    @DisplayName("예약 수정을 end-to-end로 확인한다.")
    void updateReservation() {
        Long reservationId = saveReservation("보예", "2026-06-01", "10:00", "공포");
        Long dateId = saveDate("2026-06-02");
        Long timeId = saveTime("11:00");

        String request = """
            {
                "dateId": %d,
                "timeId": %d
            }
            """.formatted(dateId, timeId);

        given().log().all()
            .contentType(ContentType.JSON)
            .body(request)
            .when().patch("/reservations/{id}", reservationId)
            .then().log().all()
            .statusCode(204);

        given()
            .contentType(ContentType.JSON)
            .param("name", "보예")
            .when().get("/reservations")
            .then()
            .statusCode(200)
            .body("reservations[0].reservationSlot.date.startWhen", is("2026-06-02"))
            .body("reservations[0].reservationSlot.time.startAt", is("11:00"));
    }

    private Long saveReservation(String name, String date, String time, String themeName) {
        return testFixture.saveReservation(name, date, time, themeName).getId();
    }

    private Long saveReservation(String name, Long reservationSlotId, ReservationStatus status) {
        return testFixture.saveReservation(name, reservationSlotId, status).getId();
    }

    private Long saveReservationSlot(Long dateId, Long timeId, Long themeId) {
        return testFixture.saveSlot(dateId, timeId, themeId).getId();
    }

    private ReservationStatus findReservationStatus(Long reservationId) {
        return testFixture.findReservationStatus(reservationId);
    }

    private Long saveTheme(String themeName) {
        Theme theme = testFixture.saveTheme(themeName);
        return theme.getId();
    }

    private Long saveDate(String date) {
        ReservationDate reservationDate = testFixture.saveDate(date);
        return reservationDate.getId();
    }

    private Long saveTime(String time) {
        ReservationTime reservationTime = testFixture.saveTime(time);
        return reservationTime.getId();
    }
}
