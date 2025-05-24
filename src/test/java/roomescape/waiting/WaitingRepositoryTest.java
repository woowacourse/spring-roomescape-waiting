package roomescape.waiting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.waiting.repository.WaitingRepository;

@ActiveProfiles("test")
@DataJpaTest
public class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("회원 ID로 대기 정보와 순위를 함께 조회할 수 있다")
    void findWaitingsWithRankByMemberId() {
        // given
        Member member1 = memberRepository.findById(1L).orElseThrow();
        Member member2 = memberRepository.findById(2L).orElseThrow();
        Member member3 = memberRepository.findById(3L).orElseThrow();
        Theme theme = themeRepository.findById(3L).orElseThrow(); // 다른 테마 사용
        ReservationTime reservationTime = reservationTimeRepository.findById(3L).orElseThrow(); // 다른 예약 시간 사용
        LocalDate date = LocalDate.now().plusDays(15); // 더 먼 미래 날짜 사용

        // 같은 테마, 같은 시간, 같은 날짜에 대기 등록 (순서대로 member1, member2, member3)
        waitingRepository.save(new Waiting(member1, reservationTime, theme, date));
        waitingRepository.save(new Waiting(member2, reservationTime, theme, date));
        waitingRepository.save(new Waiting(member3, reservationTime, theme, date));

        // when
        List<WaitingWithRank> waitingsByMember1 = waitingRepository.findWaitingsWithRankByMemberId(member1.getId());
        List<WaitingWithRank> waitingsByMember2 = waitingRepository.findWaitingsWithRankByMemberId(member2.getId());
        List<WaitingWithRank> waitingsByMember3 = waitingRepository.findWaitingsWithRankByMemberId(
            member3.getId());

        assertThat(waitingsByMember1.getFirst().getRank()).isEqualTo(1);
        assertThat(waitingsByMember2.getFirst().getRank()).isEqualTo(2);
        assertThat(waitingsByMember3.getFirst().getRank()).isEqualTo(3);
    }

}
