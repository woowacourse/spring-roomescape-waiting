package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.request.CreateReservationTimeServiceRequest;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.service.ReservationTimeOperation;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeOperation reservationTimeOperation;

    @Transactional
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

    @Transactional
    public void delete(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.getById(id);
        reservationTimeOperation.removeTime(reservationTime);
    }
}
