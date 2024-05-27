package roomescape.domain.reservation;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;

@DataJpaTest
class ReservationWaitingRepositoryTest {
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("랭킹과 함께 예약 대기 정보를 조회한다.")
    @ParameterizedTest
    @CsvSource(value = {"1, 2, 3", "2, 1, 2", "3, 3, 1"})
    @SqlGroup({
            @Sql(value = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
            @Sql("/insert-reservations-for-waiting-rank.sql")
    })
    void findWithRankByMemberIdTest(long memberId, int theme1Rank, int theme2Rank) {
        // given & when
        List<WaitingWithRank> waitingsWithRank = reservationWaitingRepository.findWithRankByMemberId(memberId);

        // then
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(waitingsWithRank.get(0).rank()).isEqualTo(theme1Rank);
        assertions.assertThat(waitingsWithRank.get(1).rank()).isEqualTo(theme2Rank);
        assertions.assertAll();
    }
}
