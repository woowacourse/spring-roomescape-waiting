package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Slot;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateException;
import roomescape.exception.PastReservationException;
import roomescape.exception.ResourceNotFoundException;
import payment.order.OrderRepository;
import roomescape.repository.ReservationDao;
import roomescape.repository.WaitingDao;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingCommandServiceTest {

    // reservation-fixture.sql 기준 (fixed clock: 2026-05-05):
    // reservation: 2026-06-05 / time1 / theme1 에 user_c 예약 존재
    // waiting: 2026-06-05 / time1 / theme1 에 user_e 대기 존재

    @Autowired
    private WaitingCommandService waitingCommandService;
    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("기존 예약이 없는 슬롯에는 대기를 생성할 수 없다.")
    void create_withoutReservation() {
        assertThatThrownBy(() ->
                waitingCommandService.create("new-user", LocalDate.of(2026, 6, 6), 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("지난 빈 슬롯에는 대기를 생성할 수 없다.")
    void create_pastWithoutReservation() {
        assertThatThrownBy(() ->
                waitingCommandService.create("new-user", LocalDate.of(2026, 4, 28), 1L, 2L))
                .isInstanceOf(PastReservationException.class);
    }

    @Test
    @DisplayName("같은 슬롯에 이미 자신의 대기가 있으면 중복 생성할 수 없다.")
    void create_duplicateWaiting() {
        assertThatThrownBy(() ->
                waitingCommandService.create("user_e", LocalDate.of(2026, 6, 5), 1L, 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("자신이 예약한 슬롯에는 대기를 생성할 수 없다.")
    void create_ownReservationConflict() {
        assertThatThrownBy(() ->
                waitingCommandService.create("user_c", LocalDate.of(2026, 6, 5), 1L, 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("결제 대기 예약에는 예약 대기를 생성할 수 없다.")
    void create_pendingPaymentReservation() {
        reservationCommandService.createPendingPaymentReservation("pending-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        assertThatThrownBy(() ->
                waitingCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("예약이 존재하고 자신의 대기가 없으면 대기 생성에 성공한다.")
    void create_success() {
        Waiting created = waitingCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 1L);

        assertThat(created.id()).isNotNull();
        assertThat(created.owner().name()).isEqualTo("new-user");
    }

    @Test
    @DisplayName("존재하지 않는 대기는 취소할 수 없다.")
    void cancel_nonExistent() {
        assertThatThrownBy(() -> waitingCommandService.cancel(999L, "user_d"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("빈 슬롯의 다음 대기 1번을 결제대기 예약으로 전환한다.")
    void promoteNextWaitingIn_promotesFirstWaiting() {
        Reservation reservation = reservationDao.findById(3L).orElseThrow();
        Slot slot = reservation.slot();
        reservationDao.deleteById(reservation.id());

        waitingCommandService.promoteNextWaitingIn(slot);

        Reservation promoted = reservationDao.findAllByName(new Member("user_e")).getFirst();
        assertThat(promoted.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(orderRepository.findAll())
                .anySatisfy(order -> assertThat(order.reservationId()).isEqualTo(promoted.id()));
        assertThat(waitingDao.findById(3L)).isEmpty();
        assertThat(waitingDao.findById(4L)).isPresent();
    }

    @Test
    @DisplayName("과거 슬롯의 대기는 예약으로 전환하지 않는다.")
    void promoteNextWaitingIn_pastSlot() {
        Reservation reservation = reservationDao.findById(1L).orElseThrow();
        Slot slot = reservation.slot();
        reservationDao.deleteById(reservation.id());

        waitingCommandService.promoteNextWaitingIn(slot);

        assertThat(reservationDao.findBySlot(slot)).isEmpty();
        assertThat(waitingDao.findById(1L)).isPresent();
    }
}
