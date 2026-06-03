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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
    private ApplicationEventPublisher eventPublisher;

    private AdminReservationService reservationService;

    @BeforeEach
    void setUp() {
        ReservationMapper mapper = new ReservationMapper(new TimeMapper(), new ThemeMapper());
        reservationService = new ReservationManagementService(
            reservationRepository, timeRepository, themeRepository, mapper, eventPublisher);
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
        void 예약을_삭제한다() {
            // given
            when(reservationRepository.existsReservationByIdAndNotDeleted(1L)).thenReturn(true);

            // when
            reservationService.deleteReservationById(1L);

            // then
            verify(reservationRepository).deleteReservationById(1L);
        }

        @Test
        void 존재하지_않는_예약_ID이면_예외가_발생한다() {
            // given
            when(reservationRepository.existsReservationByIdAndNotDeleted(999L)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> reservationService.deleteReservationById(999L))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약을 찾을 수 없습니다.");

            verify(reservationRepository, never()).deleteReservationById(any());
        }
    }
}
