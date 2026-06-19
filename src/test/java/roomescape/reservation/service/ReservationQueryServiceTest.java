package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.query.dto.ReservationWithStatusResult;
import roomescape.reservation.repository.ReservationQueryDao;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.PopularThemesResult;
import roomescape.reservation.service.dto.ReservationResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.waiting.domain.ReservationWaiting;
import roomescape.waiting.repository.ReservationWaitingRepository;

@ExtendWith(MockitoExtension.class)
class ReservationQueryServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @Mock
    private ReservationQueryDao reservationQueryDao;

    @Mock
    private roomescape.payment.repository.PaymentRepository paymentRepository;

    @InjectMocks
    private ReservationQueryService reservationQueryService;

    @Test
    @DisplayName("모든 예약 목록을 성공적으로 조회하고 DTO로 변환한다.")
    void findAll_returnsReservationResults() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation reservation = new Reservation(1L, "브라운",
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme),
                LocalDateTime.now(), true);

        given(reservationRepository.findAll()).willReturn(List.of(reservation));

        // when
        List<ReservationResult> results = reservationQueryService.findAll();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().id()).isEqualTo(1L);
        assertThat(results.getFirst().name()).isEqualTo("브라운");
        then(reservationRepository).should().findAll();
    }

    @Test
    @DisplayName("이름으로 조회 시 대기 내역이 없으면 확정 예약 내역만 정렬하여 반환한다.")
    void findAllByName_noWaitings_returnsOnlyReservations() {
        // given
        String name = "브라운";
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(15, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");

        Reservation fixedReservation1 = new Reservation(1L, name,
                new ReservationSlot(LocalDate.now().plusDays(1), time1, theme), LocalDateTime.now(), true);
        Reservation fixedReservation2 = new Reservation(2L, name,
                new ReservationSlot(LocalDate.now().plusDays(1), time2, theme), LocalDateTime.now(), true);

        given(reservationRepository.findAllByName(name)).willReturn(List.of(fixedReservation1, fixedReservation2));
        given(reservationWaitingRepository.findAllByName(name)).willReturn(List.of());

        // when
        List<ReservationWithStatusResult> results = reservationQueryService.findAllByName(name);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).id()).isEqualTo(2L);
        assertThat(results.get(1).id()).isEqualTo(1L);
        then(reservationRepository).should().findAllByName(name);
        then(reservationWaitingRepository).should().findAllByName(name);
    }

    @Test
    @DisplayName("이름으로 조회 시 확정 예약과 대기 내역이 모두 있으면 병합 및 정렬하여 순번을 포함해 반환한다.")
    void findAllByName_hasWaitings_calculatesRankAndSortsReservationsAndWaitings() {
        // given
        String name = "브라운";
        ReservationTime timeAt10 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime timeAt12 = new ReservationTime(3L, LocalTime.of(12, 0));
        ReservationTime timeAt14 = new ReservationTime(2L, LocalTime.of(14, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

        ReservationSlot confirmedSlot = new ReservationSlot(dayAfterTomorrow, timeAt12, theme);
        Reservation confirmedReservation = new Reservation(1L, name, confirmedSlot, LocalDateTime.now(), true);

        ReservationSlot waitingSlotOnDayAfterTomorrow = new ReservationSlot(dayAfterTomorrow, timeAt10, theme);
        ReservationWaiting userWaitingOnDayAfterTomorrow = new ReservationWaiting(
                10L, name, waitingSlotOnDayAfterTomorrow, LocalDateTime.now().minusMinutes(10));
        ReservationWaiting fasterWaitingOnDayAfterTomorrow = new ReservationWaiting(
                30L, "클로이", waitingSlotOnDayAfterTomorrow, LocalDateTime.now().minusMinutes(20));

        ReservationSlot waitingSlotOnTomorrow = new ReservationSlot(tomorrow, timeAt14, theme);
        ReservationWaiting userWaitingOnTomorrow = new ReservationWaiting(
                20L, name, waitingSlotOnTomorrow, LocalDateTime.now());

        given(reservationRepository.findAllByName(name)).willReturn(List.of(confirmedReservation));
        given(reservationWaitingRepository.findAllByName(name)).willReturn(
                List.of(userWaitingOnDayAfterTomorrow, userWaitingOnTomorrow));
        given(reservationWaitingRepository.findAllBySlots(any())).willReturn(
                List.of(userWaitingOnDayAfterTomorrow, userWaitingOnTomorrow, fasterWaitingOnDayAfterTomorrow));

        // when
        List<ReservationWithStatusResult> results = reservationQueryService.findAllByName(name);

        // then
        assertThat(results.get(0)).isEqualTo(ReservationWithStatusResult.from(confirmedReservation));
        assertThat(results.get(1)).isEqualTo(ReservationWithStatusResult.from(userWaitingOnTomorrow, 1L));
        assertThat(results.get(2)).isEqualTo(ReservationWithStatusResult.from(userWaitingOnDayAfterTomorrow, 2L));

        then(reservationRepository).should().findAllByName(name);
        then(reservationWaitingRepository).should().findAllByName(name);
        then(reservationWaitingRepository).should().findAllBySlots(any());
    }

    @Test
    @DisplayName("결제 대기 중인 예약은 결제 주문번호를 함께 노출한다.")
    void findAllByName_pendingPayment_exposesOrderId() {
        // given
        String name = "브라운";
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        Reservation pendingReservation = new Reservation(1L, name,
                new ReservationSlot(LocalDate.now().plusDays(1), time, theme), LocalDateTime.now(), false);

        given(reservationRepository.findAllByName(name)).willReturn(List.of(pendingReservation));
        given(reservationWaitingRepository.findAllByName(name)).willReturn(List.of());
        given(paymentRepository.findByReservationId(1L))
                .willReturn(java.util.Optional.of(roomescape.payment.domain.Payment.pending(1L, "order_xyz", 50000L, LocalDateTime.now())));

        // when
        List<ReservationWithStatusResult> results = reservationQueryService.findAllByName(name);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().orderId()).isEqualTo("order_xyz");
    }

    @Test
    @DisplayName("인기 테마 조회 시, 날짜 범위를 알맞게 계산하여 Repository를 조회한다.")
    void queryPopularThemes_queriesCorrectRange() {
        // given
        LocalDate to = LocalDate.now().minusDays(1);
        LocalDate from = to.minusDays(7).plusDays(1);
        given(reservationQueryDao.queryPopularThemes(from, to, 10)).willReturn(List.of());

        // when
        PopularThemesResult result = reservationQueryService.queryPopularThemes(7, 10);

        // then
        assertThat(result.popularThemes()).isEmpty();
        then(reservationQueryDao).should().queryPopularThemes(from, to, 10);
    }
}
