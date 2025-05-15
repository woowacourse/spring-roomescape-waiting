package roomescape.reservation.application;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.response.MyReservationServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.vo.ReservationStatus;

@Service
@RequiredArgsConstructor
public class MyReservationService {

    private final ReservationRepository reservationRepository;

    public List <MyReservationServiceResponse> getAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return reservations.stream()
            .map(reservation -> buildMyReservationServiceResponse(reservation))
            .toList();
    }

    private static MyReservationServiceResponse buildMyReservationServiceResponse(
        Reservation reservation
    ) {
        ReservationStatus reservationStatus = ReservationStatus.getStatus(
            reservation.getReservationDateTime(), LocalDateTime.now()
        );
        return MyReservationServiceResponse.from(reservation, reservationStatus);
    }
}
