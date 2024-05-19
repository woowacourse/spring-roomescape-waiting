package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.dto.WaitingWithRank;

@Transactional
class WaitingRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;


    @DisplayName("해당 날짜 이후, 회원의 예약 대기를 순번과 함께 찾는다.")
    @Test
    void findWaitingRankByMemberAndDateAfter() {
        // given
        Member member = memberRepository.findById(1L).get();
        LocalDate date = LocalDate.parse("2024-05-01");

        // when
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingRankByMemberAndDateAfter(
                member, date);

        // then
        assertThat(waitingWithRanks).hasSize(2)
                .extracting("waiting.member", "rank")
                .containsExactlyInAnyOrder(
                        tuple(member, 2L),
                        tuple(member, 1L)
                );
    }
}
