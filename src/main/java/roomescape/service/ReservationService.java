package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final JdbcReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;

    public ReservationService(JdbcReservationRepository reservationRepository,
                              JpaReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationResponse> findAllReservations(ReservationSearchParams request) {
        return reservationRepository.findReservationsWithParams(request)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse createReservation(ReservationCreate reservationInfo) {
        Reservation reservation = reservationInfo.toReservation();

        ReservationTime time = reservationTimeRepository.findById(reservation.getTimeId())
                .orElseThrow(() -> new IllegalArgumentException("예약 하려는 시간이 저장되어 있지 않습니다."));
        if (LocalDateTime.of(reservation.getDate(), time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("지나간 날짜와 시간에 대한 예약은 불가능합니다.");
        }

        if (reservationRepository.isReservationExistsByDateAndTimeIdAndThemeId(reservation)) {
            throw new IllegalArgumentException("해당 테마는 같은 시간에 이미 예약이 존재합니다.");
        }

        Reservation savedReservation = reservationRepository.insertReservation(reservation);
        return new ReservationResponse(savedReservation);
    }

    public void deleteReservation(long id) {
        if (!reservationRepository.isReservationExistsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 아이디입니다.");
        }
        reservationRepository.deleteReservationById(id);
    }
}
