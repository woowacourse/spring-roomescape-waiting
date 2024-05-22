package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.domain.Member;
import roomescape.domain.ReservationRank;

@DataJpaTest
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약내역과 순위를 반환한다.")
    @Test
    void findReservationRanksWithMember() {
        Member member = memberRepository.findById(2L).orElseThrow();

        List<ReservationRank> reservationRanksWithMember =
                reservationRepository.findReservationRanksWithMember(member);
        List<Long> pendingRanks = reservationRanksWithMember.stream()
                .filter(reservationRank -> reservationRank.getReservation().getStatus().isPending())
                .map(ReservationRank::getRank)
                .toList();
        assertAll(
                () -> assertThat(reservationRanksWithMember).hasSize(9),
                () -> assertThat(pendingRanks).hasSize(3)
        );
    }

    @DisplayName("예약 대기 중인 예약들을 조회한다.")
    @Test
    void findAllPendingOrderByDateAscTime() {
        //when
        List<ReservationRank> reservationRanks = reservationRepository.findAllPendingOrderByDateAscTime();

        //then
        assertThat(reservationRanks).hasSize(10);
    }
}
