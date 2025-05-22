package roomescape.service.waiting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.ReservationAndWaitingResponseDto;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.repository.JpaWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitingQueryServiceTest {

    @Mock
    private JpaWaitingRepository waitingRepository;

    @InjectMocks
    private WaitingQueryService waitingQueryService;

    @Test
    @DisplayName("내 예약 대기 목록을 조회한다")
    void findMyWaiting() {
        // given
        Member member = new Member(1L, "테스트", "test@test.com", Role.USER, "password");
        Theme theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Waiting waiting = new Waiting(1L, member, LocalDate.now().plusDays(1), time, theme);
        WaitingWithRank waitingWithRank = new WaitingWithRank(waiting, 1);

        when(waitingRepository.findByMemberId(1L)).thenReturn(List.of(waitingWithRank));

        // when
        List<ReservationAndWaitingResponseDto> result = waitingQueryService.findMyWaiting(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).theme()).isEqualTo("테마1");
        assertThat(result.get(0).status()).isEqualTo("1번째 예약대기");
    }

    @Test
    @DisplayName("모든 예약 대기 목록을 조회한다")
    void findAllWaiting() {
        // given
        Member member = new Member(1L, "테스트", "test@test.com", Role.USER, "password");
        Theme theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Waiting waiting = new Waiting(1L, member, LocalDate.now().plusDays(1), time, theme);

        when(waitingRepository.findAll()).thenReturn(List.of(waiting));

        // when
        List<WaitingResponseDto> result = waitingQueryService.findAllWaiting();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("테스트");
        assertThat(result.get(0).theme()).isEqualTo("테마1");
    }
} 
