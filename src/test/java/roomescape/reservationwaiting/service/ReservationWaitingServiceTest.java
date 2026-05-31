package roomescape.reservationwaiting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @Mock
    private ReservationWaitingRepository waitingRepository;
    @Mock
    private ReservationTimeService reservationTimeService;
    @Mock
    private ThemeService themeService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReservationService reservationService;
    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    private final Member member = Member.restore(1L, "user1", "test@test.com", "1234");
    private final ReservationTime time = ReservationTime.restore(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    private final Theme theme = Theme.restore(1L, "테마A", "설명", "https://a.com");
    private final LocalDate futureDate = LocalDate.now().plusDays(1);

    @Test
    @DisplayName("대기 생성 성공")
    void 대기_생성_성공() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, member, futureDate, time, theme);
        when(reservationTimeService.getById(1L)).thenReturn(time);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(any(), anyLong(), anyLong())).thenReturn(true);
        when(waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(anyLong(), any(), anyLong(), anyLong())).thenReturn(false);
        when(waitingRepository.save(any())).thenReturn(waiting);

        ReservationWaitingResponse response = reservationWaitingService.createWaiting(member,
                new ReservationWaitingRequest(futureDate, 1L, 1L));
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 사용자가 같은 슬롯에 중복 대기할 수 없다.")
    void 중복_대기_예외() {
        when(reservationTimeService.getById(1L)).thenReturn(time);
        when(themeService.getById(1L)).thenReturn(theme);
        when(reservationRepository.existsByDateAndTimeIdAndThemeId(any(), anyLong(), anyLong())).thenReturn(true);
        when(waitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(anyLong(), any(), anyLong(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> reservationWaitingService.createWaiting(member,
                new ReservationWaitingRequest(futureDate, 1L, 1L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("같은 슬롯에 중복 대기할 수 없습니다.");
    }

    @Test
    @DisplayName("대기 삭제 성공")
    void 대기_삭제_성공() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, member, futureDate, time, theme);
        when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));

        reservationWaitingService.deleteWaiting(1L, 1L);
        verify(waitingRepository).deleteById(1L);
    }

    @Test
    @DisplayName("다른 사람의 대기는 삭제할 수 없다")
    void 타인_대기_삭제_불가() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, member, futureDate, time, theme);
        when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));

        assertThatThrownBy(() -> reservationWaitingService.deleteWaiting(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("멤버 id로 대기 목록을 조회한다.")
    void 대기_목록_조회() {
        ReservationWaiting waiting = ReservationWaiting.restore(1L, member, futureDate, time, theme);
        when(waitingRepository.findByMemberId(1L)).thenReturn(List.of(waiting));
        when(waitingRepository.calculateTurn(anyLong(), any(), anyLong(), anyLong())).thenReturn(1L);

        assertThat(reservationWaitingService.getWaitingByMemberId(1L)).hasSize(1);
    }
}
