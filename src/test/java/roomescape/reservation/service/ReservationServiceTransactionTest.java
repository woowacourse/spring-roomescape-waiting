package roomescape.reservation.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.JdbcReservationRepository;
import roomescape.reservation.service.fixture.ReservationServiceFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Sql(scripts = "/concurrency-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ReservationServiceTransactionTest extends ReservationServiceFixture {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private JdbcReservationRepository reservationRepository;

    @Test
    @DisplayName("예약 취소 후 승급 중 예외가 발생하면 전체 롤백된다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cancel_rollback_when_promote_fail() {
        // given
        timeManager.setFixed(LocalDate.of(2025, 5, 10));
        ReservationTime time = insertReservationTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("레벨2 탈출", "설명", "thumbnail");

        Reservation confirmed = insertReservation("포비", LocalDate.of(2025, 5, 11), time, theme, Status.CONFIRMED);
        Reservation waiting = insertReservation("브라운", LocalDate.of(2025, 5, 11), time, theme, Status.WAITING);

        doThrow(new RuntimeException("boom"))
                .when(reservationRepository)
                .updateStatus(waiting.getId(), Status.CONFIRMED);

        // when & then
        assertThatThrownBy(() -> reservationService.cancel(confirmed.getId()))
                .isInstanceOf(RuntimeException.class);

        Reservation confirmedAfter = reservationRepository.findById(confirmed.getId()).orElseThrow();
        Reservation waitingAfter = reservationRepository.findById(waiting.getId()).orElseThrow();

        assertThat(confirmedAfter.getStatus()).isEqualTo(Status.CONFIRMED);
        assertThat(waitingAfter.getStatus()).isEqualTo(Status.WAITING);
    }

    @Test
    @DisplayName("예약 수정 후 승급 중 예외가 발생하면 전체 롤백된다.")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void editDateTime_rollback_when_promote_fail() {
        // given
        timeManager.setFixed(LocalDate.of(2025, 5, 10));
        Theme theme = insertTheme("레벨2 탈출", "설명", "thumbnail");
        ReservationTime beforeTime = insertReservationTime(LocalTime.of(10, 0));
        ReservationTime afterTime = insertReservationTime(LocalTime.of(11, 0));

        Reservation confirmed = insertReservation("포비", LocalDate.of(2025, 5, 11), beforeTime, theme, Status.CONFIRMED);
        Reservation waiting = insertReservation("브라운", LocalDate.of(2025, 5, 11), beforeTime, theme, Status.WAITING);

        doThrow(new RuntimeException("boom"))
                .when(reservationRepository)
                .updateStatus(waiting.getId(), Status.CONFIRMED);

        // when & then
        assertThatThrownBy(
                () -> reservationService.editDateTime(confirmed.getId(), LocalDate.of(2025, 5, 12), afterTime.getId(),
                        confirmed.getGuestName())).isInstanceOf(RuntimeException.class);

        Reservation confirmedAfter = reservationRepository.findById(confirmed.getId()).orElseThrow();
        Reservation waitingAfter = reservationRepository.findById(waiting.getId()).orElseThrow();

        assertThat(confirmedAfter.getStatus()).isEqualTo(Status.CONFIRMED);
        assertThat(waitingAfter.getStatus()).isEqualTo(Status.WAITING);
        assertThat(confirmedAfter.getDate()).isEqualTo(LocalDate.of(2025, 5, 11));
    }

}
