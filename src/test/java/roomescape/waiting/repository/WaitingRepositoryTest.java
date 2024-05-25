package roomescape.waiting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.test.RepositoryTest;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithOrder;

class WaitingRepositoryTest extends RepositoryTest {
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("나의 예약 대기들을 조회할 수 있다.")
    @Test
    void findByMember_idTest() {
        Reservation reservation = reservationRepository.findById(1L).get();
        Member member = memberRepository.findById(3L).get();
        Waiting waiting = new Waiting(1L, reservation, member);

        assertThat(waitingRepository.findByMember_idWithRank(3L))
                .isEqualTo(List.of(new WaitingWithOrder(waiting, 1L)));
    }
}
