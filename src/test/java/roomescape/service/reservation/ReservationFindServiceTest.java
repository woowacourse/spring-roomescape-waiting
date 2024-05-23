package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.ReservationWaitingWithRank;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationFindServiceTest {

    @Autowired
    private ReservationFindService reservationFindService;

    @Test
    @DisplayName("사용자의 예약대기가 몇번째인지 구한다.")
    void findWaitingRank() {
        List<ReservationWaitingWithRank> waitingWithRanks1 =
                reservationFindService.findMemberReservations(2L);
        List<ReservationWaitingWithRank> waitingWithRanks2 =
                reservationFindService.findMemberReservations(3L);
        assertAll(
                () -> waitingWithRanks1.get(0).getRank().equals(1),
                () -> waitingWithRanks1.get(1).getRank().equals(1),
                () -> waitingWithRanks2.get(0).getRank().equals(2),
                () -> waitingWithRanks2.get(1).getRank().equals(2)
        );
    }

}
