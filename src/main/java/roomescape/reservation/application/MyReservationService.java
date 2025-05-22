package roomescape.reservation.application;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.response.MyReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;
import roomescape.reservation.model.vo.WaitingWithRank;

@Service
@RequiredArgsConstructor
public class MyReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public List<MyReservationServiceResponse> getAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(
            memberId);
        List<MyReservationServiceResponse> response = new ArrayList<>();

        for (Reservation reservation : reservations) {
            response.add(MyReservationServiceResponse.from(reservation));
        }

        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            response.add(MyReservationServiceResponse.from(waitingWithRank));
        }

        return response;
    }
}
