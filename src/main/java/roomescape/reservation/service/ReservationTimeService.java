package roomescape.reservation.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public ReservationTimeCreateResponse create(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        ReservationTime reservationTime = reservationTimeDao.insert(reservationTimeCreateRequest);
        return ReservationTimeCreateResponse.of(reservationTime.getId(), reservationTime.getStartAt());
    }

    public List<ReservationTimeFindAllResponse> findAll() {
        return reservationTimeDao.findAll().stream()
                .map(it -> ReservationTimeFindAllResponse.of(it.getId(), it.getStartAt()))
                .toList();
    }

    public void delete(Long id) {
        reservationTimeDao.delete(id);
    }

    public ReservationTime findById(Long id) {
        return reservationTimeDao.findById(id);
    }
}
