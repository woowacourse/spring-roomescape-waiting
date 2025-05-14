package roomescape.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(final ReservationTimeDao reservationTimeDao,
                                  final ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public List<ReservationTimeResponse> getAll() {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        if (reservationTimeDao.existsByStartAt(request.startAt())) {
            throw new AlreadyInUseException("Reservation time already exists");
        }
        ReservationTime reservationTime = request.toEntity();

        ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void delete(final Long id) {
        if (reservationDao.existsByTimeId(id)) {
            throw new AlreadyInUseException("Reservation is already in use");
        }

        int updatedRow = reservationTimeDao.deleteById(id);
        if (updatedRow == 0) {
            throw new EntityNotFoundException("Reservation time not found");
        }
    }
}
