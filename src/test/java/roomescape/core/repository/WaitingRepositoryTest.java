package roomescape.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.domain.WaitingWithRank;
import roomescape.utils.TestFixture;

@DataJpaTest
class WaitingRepositoryTest {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("회원의 예약 대기가 몇 번째 순번인지 조회한다.")
    void findWaitingRankByMember() {
        final Member member = memberRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
        final ReservationTime time = reservationTimeRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
        final Theme theme = themeRepository.findById(1L).orElseThrow(IllegalArgumentException::new);
        final Waiting waiting = new Waiting(member, TestFixture.getTomorrowDate(), time, theme);
        waitingRepository.save(waiting);

        final List<WaitingWithRank> waitingWithRanks = waitingRepository.findAllWithRankByMember(member);

        assertThat(waitingWithRanks.get(0).getRank()).isZero();
    }
}
