package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateRepository;
import roomescape.reservation.domain.ReservationSlotRepository;
import roomescape.reservation.presentation.response.AdminReservationDateResponse;
import roomescape.reservation.presentation.request.CreateReservationDateRequest;
import roomescape.reservation.presentation.response.CreateReservationDateResponse;
import roomescape.reservation.presentation.response.ReservationDateResponse;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.errors.ReservationDateErrors;

@Service
@RequiredArgsConstructor
public class ReservationDateService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationDateRepository reservationDateRepository;

    public List<AdminReservationDateResponse> getAllReservationDateForAdmin() {
        return reservationDateRepository.findAll().stream()
            .map(AdminReservationDateResponse::from)
            .toList();
    }

    @Transactional
    public CreateReservationDateResponse createReservationDate(CreateReservationDateRequest request) {
        ReservationDate reservationDate = reservationDateRepository.save(request.toEntity());
        return CreateReservationDateResponse.from(reservationDate);
    }

    @Transactional
    public void deleteReservationDate(Long id) {
        if (reservationSlotRepository.countByReservationDateId(id) > 0) {
            throw new ConflictException(ReservationDateErrors.RESERVATION_DATE_IN_USE);
        }
        reservationDateRepository.deleteById(id);
    }

    public List<ReservationDateResponse> getAllReservationDate() {
        return reservationDateRepository.findAll().stream()
            .map(ReservationDateResponse::from)
            .toList();
    }
}
