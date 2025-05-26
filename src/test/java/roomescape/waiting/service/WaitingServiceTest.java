package roomescape.waiting.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.dto.request.LoginMember;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingStatus;
import roomescape.waiting.dto.request.WaitingRequest;
import roomescape.waiting.dto.response.WaitingResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class WaitingServiceTest {
    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @InjectMocks
    private WaitingService waitingService;

    @Test
    void 대기_생성_시_정상적으로_생성된다() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);
        LoginMember loginMember = new LoginMember(memberId, "포라");

        Member member = TestFixture.createMember("fora", "fora@gmail.com", "1234");
        ReservationTime time = TestFixture.createTime(10, 0);
        Theme theme = TestFixture.createTheme("테마1", "설명", "썸네일");
        Waiting waiting = new Waiting(member, date, time, theme, WaitingStatus.PENDING, LocalDateTime.of(2025, 1, 1, 10, 0));
        ReflectionTestUtils.setField(waiting, "id", 1L);

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(waitingRepository.existsByMemberIdAndDateAndTimeIdAndStatus(
                memberId, date, timeId, WaitingStatus.PENDING))
                .thenReturn(false);
        when(waitingRepository.countByDateAndThemeIdAndTimeIdAndStatus(
                date, themeId, timeId, WaitingStatus.PENDING))
                .thenReturn(0L);
        when(waitingRepository.save(any(Waiting.class)))
                .thenReturn(waiting);

        // when
        WaitingResponse response = waitingService.createWaiting(request, loginMember);

        // then
        verify(waitingRepository).save(any(Waiting.class));
        assertThat(response.date()).isEqualTo(date.toString());
        assertThat(response.theme()).isEqualTo(theme.getName());
        assertThat(response.startAt()).isEqualTo(time.getStartAt().toString());
    }

    @Test
    void 이미_대기_중인_경우_예외가_발생한다() {
        // given
        LocalDate date = LocalDate.of(2024, 3, 20);
        Long timeId = 1L;
        Long themeId = 1L;
        Long memberId = 1L;

        WaitingRequest request = new WaitingRequest(date, timeId, themeId);
        LoginMember loginMember = new LoginMember(memberId, "포라");

        Member member = TestFixture.createMember("fora", "fora@gmail.com", "1234");
        ReservationTime time = TestFixture.createTime(10, 0);
        Theme theme = TestFixture.createTheme("테마1", "설명", "썸네일");
        Waiting waiting = new Waiting(member, date, time, theme, WaitingStatus.PENDING, LocalDateTime.of(2025, 1, 1, 10, 0));
        ReflectionTestUtils.setField(waiting, "id", 1L);

        when(memberRepository.findById(memberId))
                .thenReturn(Optional.of(member));
        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(time));
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(theme));
        when(waitingRepository.existsByMemberIdAndDateAndTimeIdAndStatus(
                memberId, date, timeId, WaitingStatus.PENDING)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request, loginMember))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 대기_취소_시_정상적으로_취소된다() {
        // given
        Long waitingId = 1L;
        LocalDate date = LocalDate.of(2025, 1, 1);

        Member member = TestFixture.createMember("fora", "fora@gmail.com", "1234");
        ReservationTime time = TestFixture.createTime(10, 0);
        Theme theme = TestFixture.createTheme("테마1", "설명", "썸네일");
        Waiting waiting = new Waiting(member, date, time, theme, WaitingStatus.PENDING, LocalDateTime.of(2025, 1, 1, 10, 0));
        ReflectionTestUtils.setField(waiting, "id", 1L);

        when(waitingRepository.findById(waitingId)).thenReturn(Optional.of(waiting));

        // when
        waitingService.cancelWaiting(waitingId);

        // then
        verify(waitingRepository).save(argThat(w ->
                w.getStatus() == WaitingStatus.CANCELED));
    }
}
