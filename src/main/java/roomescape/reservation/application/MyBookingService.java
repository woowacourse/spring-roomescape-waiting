package roomescape.reservation.application;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.application.dto.response.MyBookingServiceResponse;
import roomescape.reservation.application.dto.response.MyWaitingServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.WaitingRepository;
import roomescape.reservation.model.vo.WaitingWithRank;

@Service
@RequiredArgsConstructor
public class MyBookingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public List<MyBookingServiceResponse> getAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingWithRankByMemberId(
            memberId);
        List<MyBookingServiceResponse> response = new ArrayList<>();

        for (Reservation reservation : reservations) {
            response.add(MyBookingServiceResponse.from(reservation));
        }

        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            response.add(MyBookingServiceResponse.from(waitingWithRank));
        }

        return response;
    }

    public List<MyWaitingServiceResponse> getAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
            .map(MyWaitingServiceResponse::from)
            .toList();
    }

    public void deleteById(Long id) {
        Waiting waiting = waitingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 예약 대기가 존재하지 않습니다."));
        waitingRepository.delete(waiting);
    }
}
