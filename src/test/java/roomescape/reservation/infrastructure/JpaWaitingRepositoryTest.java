package roomescape.reservation.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaWaitingRepositoryTest {

    @Autowired
    private JpaWaitingRepository jpaWaitingRepository;

    @Test
    void 특정_예약에_대한_모든_대기자를_조회할_수_있다() {
        List<Waiting> result = jpaWaitingRepository.findByReservationId(1L);

        assertThat(result)
            .extracting(w -> w.getMember().getId())
            .containsExactly(2L, 3L);
    }

    @Test
    void 예약과_회원ID로_대기_존재여부를_확인할_수_있다() {
        assertThat(jpaWaitingRepository.existsByReservationIdAndMemberId(1L, 2L)).isTrue();
        assertThat(jpaWaitingRepository.existsByReservationIdAndMemberId(1L, 3L)).isTrue();
        assertThat(jpaWaitingRepository.existsByReservationIdAndMemberId(1L, 1L)).isFalse();
    }

    @Test
    void 회원ID로_대기목록과_순위를_조회할_수_있다() {
        List<WaitingWithRank> waitings = jpaWaitingRepository.findByMemberId(2L);

        assertThat(waitings).hasSize(1);
        WaitingWithRank waiting = waitings.get(0);

        assertThat(waiting.getWaiting().getMember().getId()).isEqualTo(2L);
        assertThat(waiting.getRank()).isEqualTo(1);
    }


}