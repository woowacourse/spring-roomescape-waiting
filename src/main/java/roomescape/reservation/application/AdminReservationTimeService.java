package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.request.CreateReservationTimeServiceRequest;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.service.ReservationTimeValidator;
import roomescape.global.exception.BusinessRuleViolationException;

@Service
@RequiredArgsConstructor
public class AdminReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeValidator reservationTimeValidator;

    public ReservationTimeServiceResponse create(CreateReservationTimeServiceRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.save(request.toReservationTime());
        return ReservationTimeServiceResponse.withoutBook(reservationTime);
    }

    public List<ReservationTimeServiceResponse> getAll() {
        List<ReservationTime> allTimes = reservationTimeRepository.getAll();
        return allTimes.stream()
                .map(ReservationTimeServiceResponse::withoutBook)
                .toList();
    }

    public void delete(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.getById(id);
        try {
            reservationTimeValidator.validateNotInUse(id);
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }

        reservationTimeRepository.remove(reservationTime);
    }
}
