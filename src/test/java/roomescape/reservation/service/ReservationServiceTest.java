package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.business.BusinessException;
import roomescape.exception.business.PastTimeCancelException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationTimeService reservationTimeService;
    @Mock
    private ThemeService themeService;
    @InjectMocks
    private ReservationService reservationService;

    private Member member;
    private ReservationTime time;
    private Theme theme;
    private LocalDate futureDate;
    private LocalDate pastDate;

    @BeforeEach
    void setUp() {
        member = Member.restore(1L, "user1", "test@test.com", "1234");
        time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        theme = Theme.restore(1L, "테마A", "설명", "https://a.com");
        futureDate = LocalDate.now().plusDays(1);
        pastDate = LocalDate.now().minusDays(1);
    }

    @Test
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        Reservation reservation = Reservation.restore(1L, member, futureDate, time, theme);
        when(reservationTimeService.getById(1L)).thenReturn(time);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(any(), anyLong(), anyLong())).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(reservation);

        ReservationResponse response = reservationService.createReservation(member, new ReservationRequest(futureDate, 1L, 1L));
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("중복 예약 생성 시 예외 발생")
    void 중복_예약_예외() {
        when(reservationTimeService.getById(1L)).thenReturn(time);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(any(), anyLong(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(member, new ReservationRequest(futureDate, 1L, 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 예약된 시간입니다.");
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void 예약_삭제_성공() {
        Reservation reservation = Reservation.restore(1L, member, futureDate, time, theme);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L, 1L);
        verify(reservationRepository).deleteById(1L);
    }

    @Test
    @DisplayName("이미 지난 예약은 취소할 수 없다")
    void 과거_예약_취소_불가() {
        Reservation pastReservation = Reservation.restore(1L, member, pastDate, time, theme);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pastReservation));

        assertThatThrownBy(() -> reservationService.deleteReservation(1L, 1L))
                .isInstanceOf(PastTimeCancelException.class);
    }

    @Test
    @DisplayName("다른 사람의 예약은 삭제할 수 없다")
    void 타인_예약_삭제_불가() {
        Reservation reservation = Reservation.restore(1L, member, futureDate, time, theme);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.deleteReservation(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }
}
