package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservation.WaitingWithRank;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("예약 대기 순서와 함께 조회")
    @Sql(value = "/init_data/reservationData.sql", executionPhase = BEFORE_TEST_METHOD)
    @Test
    void findWaitingsWithRankByMemberId() {
        List<WaitingWithRank> waitingsWithRankByMemberId = waitingRepository.findWaitingsWithRankByMemberId(5L);
        WaitingWithRank waitingWithRank = waitingsWithRankByMemberId.get(0);
        assertThat(waitingWithRank.getRank()).isEqualTo(4);
    }
}
