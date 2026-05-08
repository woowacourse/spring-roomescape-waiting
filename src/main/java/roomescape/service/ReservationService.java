package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationResponse;
import roomescape.repository.ReservationDao;
import roomescape.dto.ReservationRequest;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;

    public ReservationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }


    public ReservationResponse save(ReservationRequest request) {
        Long id = reservationDao.save(request.name(), request.date(), request.timeId(), request.themeId());
        Reservation reservation = reservationDao.findById(id);
        return ReservationResponse.from(reservation);

    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationDao.findByUserName(username).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationDao.delete(id);
    }
}
