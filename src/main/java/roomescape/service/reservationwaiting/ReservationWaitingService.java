package roomescape.service.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {
    public ReservationWaitingService(ReservationRepository reservationRepository,
                                     ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;



    public ReservationWaiting save(String name, LocalDate date, Long themeId, Long timeId) {
        Long reservationId = reservationRepository.findReservationIdByDateAndThemeIdAndTimeId(
                date, themeId, timeId
        );
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("[ERROR] 예약 정보가 없으면 대기 생성이 불가능합니다."));

        ReservationWaiting nonIdReservationWaiting = ReservationWaiting.createNew(reservation, name, LocalTime.now());
        return reservationWaitingRepository.save(nonIdReservationWaiting);
    }
}
