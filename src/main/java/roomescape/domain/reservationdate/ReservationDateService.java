package roomescape.domain.reservationdate;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationdate.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.dto.ReservationDateCreationRequest;
import roomescape.domain.reservationdate.dto.ReservationDateCreationResponse;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationDateService {

    private final ReservationRepository reservationRepository;
    private final ReservationDateRepository reservationDateRepository;

    public List<AdminReservationDateResponse> getAllReservationDateForAdmin() {
        return reservationDateRepository.findAll().stream()
            .map(AdminReservationDateResponse::from)
            .toList();
    }

    public ReservationDateCreationResponse createReservationDate(ReservationDateCreationRequest request) {
        if (reservationDateRepository.existsByPlayDay(request.playDay())) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_DUPLICATED);
        }
        ReservationDate reservationDate = reservationDateRepository.save(request.toEntity());
        return ReservationDateCreationResponse.from(reservationDate);
    }

    public void deleteReservationDate(Long id) {
        ReservationDate reservationDate = findById(id);
        if (reservationRepository.countByDateId(id) > 0) {
            throw new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_IN_USE);
        }
        reservationDateRepository.delete(reservationDate);
    }

    public List<ReservationDateResponse> getAllAvailableReservationDate() {
        return reservationDateRepository.findAll().stream()
            .filter(reservationDate -> reservationDate.isAvailable(LocalDate.now()))
            .map(ReservationDateResponse::from)
            .toList();
    }

    public ReservationDate findById(Long id) {
        return reservationDateRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ReservationDateErrorCode.RESERVATION_DATE_NOT_EXIST));
    }
}
