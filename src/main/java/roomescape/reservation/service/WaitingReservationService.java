package roomescape.reservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.MyReservationWithStatus;
import roomescape.reservation.domain.repository.MemberReservationRepository;

import java.util.List;

@Service
public class WaitingReservationService {

    @Autowired
    private final MemberReservationRepository memberReservationRepository;

    public WaitingReservationService(MemberReservationRepository memberReservationRepository) {
        this.memberReservationRepository = memberReservationRepository;
    }

    public List<MyReservationResponse> handleWaitingOrder(List<MyReservationWithStatus> myReservationWithStatuses) {
        return myReservationWithStatuses
                .stream()
                .map(this::handler)
                .toList();
    }

    private MyReservationResponse handler(MyReservationWithStatus myReservationWithStatus) {
        if (myReservationWithStatus.status().isWaiting()) {
            int waitingCount = memberReservationRepository.countWaitingMemberReservation(myReservationWithStatus.memberReservationId());
            return new MyReservationResponse(
                    myReservationWithStatus.memberReservationId(),
                    myReservationWithStatus.themeName(),
                    myReservationWithStatus.date(),
                    myReservationWithStatus.time(),
                    waitingCount + "번째 " + myReservationWithStatus.status().getStatus()
            );
        }
        return MyReservationResponse.from(myReservationWithStatus);
    }
}
