package roomescape.service.waiting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.waiting.ReservationWaitingDuplicateException;
import roomescape.exception.waiting.WaitingAlreadyExistException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepsitory;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepsitory waitingRepsitory;
    @Mock
    private ReservationTimeRepository timeRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    @DisplayName("정상적으로 대기를 생성할 수 있다")
    void createWaiting_success() {
        // given
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        Member member = new Member(1L, "테스터", "test@email.com", "pw", Role.USER);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(any(), any(), any(), any())).thenReturn(
                false);
        when(waitingRepsitory.existsByDateAndTimeIdAndThemeIdAndMemberId(any(), any(), any(), any())).thenReturn(false);
        when(waitingRepsitory.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        WaitingResponse response = waitingService.create(request, member);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("이미 예약이 있으면 예외 발생")
    void createWaiting_duplicateReservation() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        Member member = new Member(1L, "테스터", "test@email.com", "pw", Role.USER);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(any(), any(), any(), any())).thenReturn(
                true);

        assertThatThrownBy(() -> waitingService.create(request, member))
                .isInstanceOf(ReservationWaitingDuplicateException.class);
    }

    @Test
    @DisplayName("이미 대기가 있으면 예외 발생")
    void createWaiting_duplicateWaiting() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().plusDays(1), 1L, 1L);
        Member member = new Member(1L, "테스터", "test@email.com", "pw", Role.USER);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(any(), any(), any(), any())).thenReturn(
                false);
        when(waitingRepsitory.existsByDateAndTimeIdAndThemeIdAndMemberId(any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> waitingService.create(request, member))
                .isInstanceOf(WaitingAlreadyExistException.class);
    }

    @Test
    @DisplayName("과거 시간에 대기 생성 시 예외 발생")
    void createWaiting_pastTime() {
        ReservationRequest request = new ReservationRequest(LocalDate.now().minusDays(1), 1L, 1L);
        Member member = new Member(1L, "테스터", "test@email.com", "pw", Role.USER);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        when(timeRepository.findById(1L)).thenReturn(Optional.of(time));
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));

        assertThatThrownBy(() -> waitingService.create(request, member))
                .isInstanceOf(ReservationInPastException.class);
    }
}
