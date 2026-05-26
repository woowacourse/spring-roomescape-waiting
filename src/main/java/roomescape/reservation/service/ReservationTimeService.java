package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationTimeDAO;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeDAO reservationTimeDAO;

    public ReservationTimeService(ReservationTimeDAO reservationTimeDAO) {
        this.reservationTimeDAO = reservationTimeDAO;
    }

    public ReservationTimeCreateResponse create(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        ReservationTime reservationTime = reservationTimeDAO.insert(reservationTimeCreateRequest);
        return ReservationTimeCreateResponse.of(reservationTime.getId(), reservationTime.getStartAt());
    }

    public List<ReservationTimeFindAllResponse> findAll() {
        return reservationTimeDAO.findAll().stream()
                .map(it -> ReservationTimeFindAllResponse.of(it.getId(), it.getStartAt()))
                .toList();
    }

    public void delete(Long id) {
        reservationTimeDAO.delete(id);
    }

    public ReservationTime findById(Long id) {
        return reservationTimeDAO.findById(id);
    }
}
