package roomescape.feature.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import roomescape.feature.payment.PaymentApprover;
import roomescape.feature.reservation.cancel.SlotReleasedEvent;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.reservation.mapper.ReservationMapper;
import roomescape.feature.reservation.repository.ReservationRepository;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.theme.repository.ThemeRepository;
import roomescape.feature.time.domain.Time;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.feature.time.repository.TimeRepository;
import roomescape.global.domain.EntityStatus;
import roomescape.global.error.exception.GeneralException;

@ExtendWith(MockitoExtension.class)
class AdminReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeRepository timeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private PaymentApprover paymentApprover;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AdminReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationMapper mapper = new ReservationMapper(new TimeMapper(), new ThemeMapper());
        reservationService = new ReservationManagementService(
            reservationRepository, timeRepository, themeRepository, paymentApprover, mapper, eventPublisher);
    }

    private Time timeWithId(Long id) {
        return Time.reconstruct(id, LocalTime.of(10, 0), EntityStatus.ACTIVE);
    }

    private Theme themeWithId(Long id) {
        return Theme.reconstruct(id, "테마 이름", "테마 설명", "https://example.com/theme.png", EntityStatus.ACTIVE);
    }

    @Nested
    class 예약_목록_조회 {

        @Test
        void 예약이_없으면_빈_목록을_반환한다() {
            when(reservationRepository.findAllReservations()).thenReturn(List.of());

            assertThat(reservationService.getReservations()).isEmpty();
        }

        @Test
        void 미래_활성_예약은_EDITABLE_상태로_반환한다() {
            // given
            LocalDate futureDate = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation reservation = Reservation.reconstruct(
                1L, new ReserverName("예약자"), futureDate, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findAllReservations()).thenReturn(List.of(reservation));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.EDITABLE);
        }

        @Test
        void 취소된_예약은_CANCELED_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation canceled = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.CANCELED);
            when(reservationRepository.findAllReservations()).thenReturn(List.of(canceled));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.CANCELED);
        }

        @Test
        void 삭제된_예약은_DELETED_상태로_반환한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation deleted = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.DELETED);
            when(reservationRepository.findAllReservations()).thenReturn(List.of(deleted));

            // when
            List<ReservationResponseDto> result = reservationService.getReservations();

            // then
            assertThat(result.getFirst().status()).isEqualTo(ReservationEditableStatus.DELETED);
        }
    }

    @Nested
    class 예약_삭제 {

        @Test
        void 예약을_DELETED_상태로_변경한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Reservation active = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, timeWithId(1L), themeWithId(1L), ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L)).thenReturn(Optional.of(active));

            // when
            reservationService.deleteReservationById(1L);

            // then
            ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.DELETED);
        }

        @Test
        void 활성_예약을_삭제하면_대기_승격_이벤트를_발행한다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Time time = timeWithId(1L);
            Theme theme = themeWithId(1L);
            Reservation active = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, time, theme, ReservationStatus.ACTIVE);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L)).thenReturn(Optional.of(active));

            // when
            reservationService.deleteReservationById(1L);

            // then
            ArgumentCaptor<SlotReleasedEvent> captor =
                ArgumentCaptor.forClass(SlotReleasedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().slotKey().timeId()).isEqualTo(time.getId());
            assertThat(captor.getValue().slotKey().themeId()).isEqualTo(theme.getId());
            assertThat(captor.getValue().slotKey().date()).isEqualTo(date);
        }

        @Test
        void 대기_예약을_삭제하면_이벤트를_발행하지_않는다() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            Reservation waiting = Reservation.reconstruct(
                1L, new ReserverName("예약자"), date, timeWithId(1L), themeWithId(1L), ReservationStatus.WAITING);
            when(reservationRepository.findReservationByIdAndNotDeleted(1L)).thenReturn(Optional.of(waiting));

            // when
            reservationService.deleteReservationById(1L);

            // then
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.findReservationByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reservationService.deleteReservationById(999L))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");

            verify(reservationRepository, never()).update(any());
        }
    }
}
