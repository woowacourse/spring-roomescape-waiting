package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.domain.WaitingWithRank;
import roomescape.reservation.waiting.repository.WaitingRepository;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-time-data.sql", "/test-theme-data.sql", "/test-member-data.sql", "/test-reservation-data.sql",
        "/test-waiting-data.sql"})
public class WaitingJPARepositoryTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @DisplayName("모든 예약 대기 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<Waiting> waitings = waitingRepository.findAll();
        // then
        assertThat(waitings).hasSize(2);
    }

    @DisplayName("멤버 ID로 예약 대기 우선 순위를 조회할 수 있다.")
    @Test
    void testFindWaitingsWithRankByMemberId() {
        // given
        // when
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(1L);
        WaitingWithRank waitingWithRank = waitingWithRanks.getFirst();
        // then
        assertThat(waitingWithRanks).hasSize(1);
        assertThat(waitingWithRank.getRank()).isEqualTo(0);
        assertThat(waitingWithRank.getWaiting().getMember().getId()).isEqualTo(1L);
        assertThat(waitingWithRank.getWaiting().getDate()).isEqualTo(LocalDate.now().plusDays(1));
    }
}
