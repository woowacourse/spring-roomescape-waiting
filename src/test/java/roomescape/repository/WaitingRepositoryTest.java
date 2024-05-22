package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.WaitingRank;
import roomescape.domain.reservation.WaitingRepository;

@Transactional
class WaitingRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 대기 내역을 순위와 함께 찾는다.")
    @Test
    void findWaitingReservations() {
        // given
        Member member = memberRepository.findById(1L).get();
        // when
        List<WaitingRank> waitingReservations = waitingRepository.findRankByMemberAndDateGreaterThanEqual(member,
                LocalDate.parse("2024-05-30"));

        // then
        assertThat(waitingReservations).hasSize(2)
                .extracting("rank")
                .containsExactly(2L, 1L);
    }
}
