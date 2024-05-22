package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.WaitingWithRank;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("예약 대기 순서와 함께 조회")
    @Sql("/init_data/reservationData.sql")
    @Test
    void findWaitingsWithRankByMemberId() {
        List<WaitingWithRank> waitingsWithRankByMemberId = waitingRepository.findWaitingsWithRankByMemberId(5L);
        WaitingWithRank waitingWithRank = waitingsWithRankByMemberId.get(0);
        assertThat(waitingWithRank.getRank()).isEqualTo(4);
    }
}
