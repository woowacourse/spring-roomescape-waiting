package roomescape.reservation.service;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.MemberReservationResponse;

@SpringBootTest
@Transactional
class CompareQueryStrategy {

    @Autowired
    ReservationService reservationService;

    @DisplayName("쿼리 한방 서브 쿼리 이용")
    @Test
    void testWiSubQueryStrategy() {
        long start = System.currentTimeMillis();
        List<MemberReservationResponse> memberReservationWithWaitingStatus = reservationService.findMemberReservationWithSubQuery(
                1L);
        System.out.println("멤버의 예약 개수 = " + memberReservationWithWaitingStatus.size());
        long endTime = System.currentTimeMillis();
        System.out.println("서브 쿼리 조회 시간" + (endTime - start) + "ms");
    }

    @DisplayName("쿼리 두방")
    @Test
    void testWithNoSubQueryStrategy() {
        long start = System.currentTimeMillis();
        List<MemberReservationResponse> memberReservationWithWaitingStatus = reservationService.findMemberReservationWithTwoSimpleQuery(
                1L);
        System.out.println("멤버의 예약 개수 = " + memberReservationWithWaitingStatus.size());
        long endTime = System.currentTimeMillis();
        System.out.println("쿼리 두방 조회 시간: " + (endTime - start) + "ms");
    }


    @DisplayName("쿼리 한방 집계 함수 이용")
    @Test
    void testWithSubQueryStrategy() {
        long start = System.currentTimeMillis();
        List<MemberReservationResponse> memberReservationWithWaiting = reservationService.findMemberReservationWithJoinAndGroupBy(
                1L);
        long endTime = System.currentTimeMillis();
        System.out.println("조인 쿼리 조회 시간: " + (endTime - start) + "ms");
    }
}
