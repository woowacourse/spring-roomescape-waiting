package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.exception.DuplicateException;
import roomescape.exception.ResourceNotFoundException;
import payment.PaymentStatus;
import payment.order.Order;
import payment.order.OrderRepository;
import roomescape.repository.ReservationDao;
import roomescape.repository.WaitingDao;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationCommandServiceTest {

    // reservation-fixture.sql 기준 (fixed clock: 2026-05-05):
    // id=2: user_b / 2026-06-05 / time2 / theme1 (미래)
    // id=3: user_c / 2026-06-05 / time1 / theme1 (미래)
    // 2026-06-05 / time1 / theme2 슬롯은 비어 있음

    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private WaitingQueryService waitingQueryService;
    @Autowired
    private OrderRepository orderRepository;
    @MockitoSpyBean
    private ReservationDao reservationDao;

    @Test
    @DisplayName("이미 예약된 슬롯에는 예약을 생성할 수 없다.")
    void createDuplicateSlot() {
        assertThatThrownBy(() ->
                reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("비어 있는 슬롯에는 예약 생성에 성공한다.")
    void createSuccess() {
        Reservation created = reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        assertThat(created.id()).isNotNull();
        assertThat(created.owner().name()).isEqualTo("new-user");
    }

    @Test
    @DisplayName("사용자 예약 생성 시 결제 대기 예약과 주문을 저장한다.")
    void createPendingReservationAndOrder() {
        PendingReservation pending = reservationCommandService.createPendingPaymentReservation(
                "new-user",
                LocalDate.of(2026, 6, 5),
                1L,
                2L);

        Reservation created = pending.reservation();
        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(pending.orderId()).startsWith("order_");
        assertThat(pending.amount()).isEqualTo(5_000L);
        assertThat(pending.orderName()).isEqualTo("예약없는테마 예약");
        assertThat(reservationDao.findAllByName(new Member("new-user"))).hasSize(1);
        Order order = orderRepository.findByOrderId(pending.orderId()).orElseThrow();
        assertThat(order.reservationId()).isEqualTo(created.id());
        assertThat(order.amount()).isEqualTo(5_000L);
        assertThat(order.status()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("예약 슬롯이 이미 점유되어 있으면 결제 주문을 저장하지 않는다.")
    void createDuplicateSlotDoesNotSaveOrder() {
        assertThatThrownBy(() ->
                reservationCommandService.createPendingPaymentReservation(
                        "new-user",
                        LocalDate.of(2026, 6, 5),
                        1L,
                        1L))
                .isInstanceOf(DuplicateException.class);

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약은 취소할 수 없다.")
    void cancelNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.cancel(999L, "user_b"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 변경할 수 없다.")
    void updateNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.update(999L, "user_b", LocalDate.of(2026, 7, 1), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 슬롯으로는 변경할 수 없다.")
    void updateToDuplicateSlot() {
        // id=2(user_b)를 id=3(user_c)이 점유한 2026-06-05/time1/theme1로 변경 시도
        assertThatThrownBy(() ->
                reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 6, 5), 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("예약 취소 시 같은 슬롯의 대기 1번이 결제대기 예약으로 전환되고 대기 목록에서 사라진다.")
    void cancelPromotesFirstWaiting() {
        reservationCommandService.cancel(3L, "user_c");

        Reservation promoted = reservationDao.findAllByName(new Member("user_e")).getFirst();
        assertThat(promoted.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(orderRepository.findAll())
                .anySatisfy(order -> assertThat(order.reservationId()).isEqualTo(promoted.id()));
        assertThat(waitingDao.findById(3L)).isEmpty();
    }

    @Test
    @DisplayName("대기 없는 예약 취소 시 예약만 삭제된다.")
    void cancelWithoutWaitingDeletesReservationOnly() {
        Reservation reservation = reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        reservationCommandService.cancel(reservation.id(), "new-user");

        assertThat(reservationDao.findById(reservation.id())).isEmpty();
        assertThat(waitingDao.findById(2L)).isPresent();
    }

    @Test
    @DisplayName("대기 N명 중 1번만 결제대기 예약으로 전환되고 나머지 대기의 순번이 당겨진다.")
    void cancelPromotesOnlyFirstWaiting() {
        reservationCommandService.cancel(3L, "user_c");

        assertThat(reservationDao.findAllByName(new Member("user_e")).getFirst().status())
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservationDao.findAllByName(new Member("user_b"))).hasSize(1);
        assertThat(waitingDao.findById(4L)).isPresent();
        assertThat(waitingQueryService.getByName("user_b").getFirst().rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 삭제 시 같은 슬롯의 대기 1번이 결제대기 예약으로 전환된다.")
    void deletePromotesFirstWaiting() {
        reservationCommandService.delete(3L);

        assertThat(reservationDao.findAllByName(new Member("user_e")).getFirst().status())
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(waitingDao.findById(3L)).isEmpty();
    }

    @Test
    @DisplayName("과거 예약을 삭제하면 같은 슬롯의 대기를 승격하지 않는다.")
    void deletePastReservationDoesNotPromote() {
        reservationCommandService.delete(1L);

        assertThat(reservationDao.findById(1L)).isEmpty();
        assertThat(waitingDao.findById(1L)).isPresent();
        assertThat(reservationDao.findAllByName(new Member("user_d"))).isEmpty();
    }

    @Test
    @DisplayName("승격 중 예약 저장 실패 시 예약 삭제와 대기 삭제가 함께 롤백된다.")
    void cancelRollsBackWhenPromotionFails() {
        doThrow(new DuplicateException("승격 실패"))
                .when(reservationDao)
                .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationCommandService.cancel(3L, "user_c"))
                .isInstanceOf(DuplicateException.class);

        assertThat(reservationDao.findById(3L)).isPresent();
        assertThat(waitingDao.findById(3L)).isPresent();
    }

    @Test
    @DisplayName("예약 변경 시 비워진 옛 슬롯의 대기 1번이 결제대기 예약으로 전환된다.")
    void updatePromotesWaitingInVacatedSlot() {
        reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 6, 5), 3L);

        assertThat(reservationDao.findAllByName(new Member("user_d")).getFirst().status())
                .isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(waitingDao.findById(2L)).isEmpty();
    }

    @Test
    @DisplayName("같은 슬롯으로 변경하면 옛 슬롯이 비지 않으므로 승격하지 않는다.")
    void updateToSameSlotDoesNotPromote() {
        reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 6, 5), 2L);

        assertThat(waitingDao.findById(2L)).isPresent();
        assertThat(reservationDao.findAllByName(new Member("user_d"))).isEmpty();
    }
}
