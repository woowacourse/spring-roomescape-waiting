package roomescape.reservation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationDao reservationDao;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationDao reservationDao
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationDao = reservationDao;
    }

    public List<ReservationTimeResponse> getAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new AlreadyInUseException("Reservation time already exists");
        }
        ReservationTime reservationTime = request.toEntity();

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void delete(final Long id) {
        if (reservationDao.existsByTimeId(id)) {
            throw new AlreadyInUseException("Reservation is already in use");
        }
        reservationTimeRepository.deleteById(id);
    }
}
