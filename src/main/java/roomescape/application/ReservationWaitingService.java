package roomescape.application;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.dto.WaitingWithRankDto;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationWaitingService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<MyReservationResponse> getMyReservationAndWaitingWithRanks(long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        List<WaitingWithRankDto> waitingsWithRank = waitingRepository.findWaitingsWithRankByMemberId(memberId);

        return Stream.concat(
                        reservations.stream().map(MyReservationResponse::from),
                        waitingsWithRank.stream().map(MyReservationResponse::from))
                .sorted(Comparator.comparing(MyReservationResponse::date).thenComparing(MyReservationResponse::time))
                .toList();
    }
}
