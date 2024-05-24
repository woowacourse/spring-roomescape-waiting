package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.InitialReservationFixture.RESERVATION_2;
import static roomescape.InitialWaitingFixture.MEMBER_2_INITIAL_WAITING_COUNT;
import static roomescape.InitialWaitingFixture.WAITING_1;
import static roomescape.InitialWaitingFixture.WAITING_2;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Waiting;
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

    @Test
    @DisplayName("특정 예약에 대해 1번 예약 대기를 가져온다.")
    void findFirstWaiting() {
        Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndReservationTimeAndThemeOrderByIdAsc(
                WAITING_1.getDate(),
                WAITING_1.getReservationTime(),
                WAITING_1.getTheme()
        );

        assertThat(firstWaiting.get()).isEqualTo(WAITING_1);
    }

    @Test
    @DisplayName("특정 예약에 대해 1번 예약 대기를 가져오려는데, 예약 대기가 없으면 비어있는 Optional을 반환한다.")
    void findEmptyOptionalIfNoWaiting() {
        Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndReservationTimeAndThemeOrderByIdAsc(
                RESERVATION_2.getDate(),
                RESERVATION_2.getReservationTime(),
                RESERVATION_2.getTheme()
        );

        assertThat(firstWaiting.isEmpty()).isTrue();
    }
}
