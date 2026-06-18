package roomescape.domain.reservationdate;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationdate.admin.dto.AdminReservationDateResponse;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateRequest;
import roomescape.domain.reservationdate.admin.dto.CreateReservationDateResponse;
import roomescape.domain.reservationdate.dto.ReservationDateResponse;
import roomescape.domain.reservationslot.JpaReservationSlotRepository;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;

@Service
@RequiredArgsConstructor
public class ReservationDateService {

    private final JpaReservationSlotRepository reservationSlotRepository;
    private final JpaReservationDateRepository reservationDateRepository;

    public List<AdminReservationDateResponse> getAllReservationDateForAdmin() {
        return reservationDateRepository.findAll().stream()
            .map(AdminReservationDateResponse::from)
            .toList();
    }

    public CreateReservationDateResponse createReservationDate(CreateReservationDateRequest request) {
        ReservationDate reservationDate = reservationDateRepository.save(request.toEntity());
        return CreateReservationDateResponse.from(reservationDate);
    }

    public void deleteReservationDate(Long id) {
        if (reservationSlotRepository.countByDateId(id) > 0) {
            throw new ConflictException(ReservationDateErrors.RESERVATION_DATE_IN_USE);
        }
        reservationDateRepository.deleteById(id);
    }

    public List<ReservationDateResponse> getAllReservationDate() {
        return reservationDateRepository.findAll().stream()
            .map(ReservationDateResponse::from)
            .toList();
    }

    public ReservationDate findDateByIdOrThrow(Long dateId) {
        return reservationDateRepository.findById(dateId)
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }
}
