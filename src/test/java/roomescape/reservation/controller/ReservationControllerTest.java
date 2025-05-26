package roomescape.reservation.controller;

import io.restassured.RestAssured;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.AdminTestDataConfig;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.fixture.WaitingFixture;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {
        ReservationTimeTestDataConfig.class,
        ThemeTestDataConfig.class,
        AdminTestDataConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    private static ReservationTime savedReservationTime;
    private static Theme savedTheme;
    private static User savedUser;


    @LocalServerPort
    int port;

    @BeforeEach
    void restAssuredSetUp() {
        RestAssured.port = port;
    }

    @BeforeAll
    public static void setUp(@Autowired ReservationTimeTestDataConfig reservationTimeTestDataConfig,
                             @Autowired ThemeTestDataConfig themeTestDataConfig,
                             @Autowired AdminTestDataConfig adminTestDataConfig
    ) {
        savedReservationTime = reservationTimeTestDataConfig.getSavedReservationTime();
        savedTheme = themeTestDataConfig.getSavedTheme();
        savedUser = adminTestDataConfig.getSavedAdmin();
    }

    @Nested
    @DisplayName("DELETE /reservations/{reservationId} 요청")
    class cancelAndApproveWaiting {

        private Reservation createAndSaveDefaultReservationByBookedStatus(LocalDate date) {
            return reservationRepository.save(createDefaultReservationByBookedStatus(date));
        }

        private Reservation createDefaultReservationByBookedStatus(LocalDate date) {
            return ReservationFixture.createByBookedStatus(date, savedReservationTime, savedTheme, savedUser);
        }

        private Waiting createAndSaveWaitingByReservation(Reservation reservation) {
            Waiting waiting = createWaitingByReservation(reservation);
            return waitingRepository.save(waiting);
        }

        private Waiting createWaitingByReservation(Reservation reservation) {
            return WaitingFixture.create(reservation.getDate(),
                    reservation.getReservationTime(),
                    reservation.getTheme(),
                    reservation.getUser());
        }

        @DisplayName("존재하는 예약 id로 요청할 때 204 NO_CONTENT 함께 예약을 삭제 하고 대기 예약을 승인한다")
        @Test
        void cancelAndApproveWaiting_success_byExistingReservationId() {
            // given
            Reservation savedReservation = createAndSaveDefaultReservationByBookedStatus(LocalDate.now().plusDays(1));
            Waiting savedWaiting = createAndSaveWaitingByReservation(savedReservation);

            // when & then
            RestAssured.given().log().all()
                    .when().delete("/reservations/" + savedReservation.getId())
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

            Assertions.assertThat(reservationRepository.findAll()).hasSize(1);
            Assertions.assertThat(waitingRepository.findAll()).hasSize(0);
            Assertions.assertThat(savedWaiting.getDate()).isEqualTo(savedReservation.getDate());
        }
    }
}
