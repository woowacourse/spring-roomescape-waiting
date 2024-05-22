package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.InitialWaitingFixture.MEMBER_2_INITIAL_WAITING_COUNT;
import static roomescape.InitialWaitingFixture.WAITING_1;
import static roomescape.InitialWaitingFixture.WAITING_2;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.WaitingWithRank;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    @DisplayName("특정 회원이 가진 예약 대기들을 대기 순번과 함께 조회한다.")
    void findWaitingsWithRankByMemberId() {
        List<WaitingWithRank> waitingsWithRanks =
                waitingRepository.findWaitingsWithRankByMemberId(WAITING_1.getMember().getId());
        WaitingWithRank waiting1WithRank = waitingsWithRanks.get(0);

        assertAll(
                () -> assertThat(waitingsWithRanks.size()).isEqualTo(MEMBER_2_INITIAL_WAITING_COUNT),
                () -> assertThat(waiting1WithRank.rank()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("예약 대기를 삭제하면 뒤따라오는 예약 대기의 순번이 당겨진다.")
    void WaitingReorderedByDeletingWaiting() {
        waitingRepository.deleteById(WAITING_1.getId());

        List<WaitingWithRank> waitingWithRank =
                waitingRepository.findWaitingsWithRankByMemberId(WAITING_2.getMember().getId());
        WaitingWithRank waiting2WithRank = waitingWithRank.get(0);

        assertThat(waiting2WithRank.rank()).isEqualTo(1);
    }
}
