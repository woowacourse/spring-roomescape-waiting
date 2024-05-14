package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.ReservationTime;
import roomescape.dto.reservation.AvailableReservationTimeResponse;
import roomescape.dto.reservation.AvailableReservationTimeSearch;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dao.ReservationTimeDao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(final ReservationTimeDao reservationTimeDao,
                                  final ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public ReservationTimeResponse create(final ReservationTime reservationTime) {
        final ReservationTime savedReservationTime = reservationTimeDao.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationTimeResponse findById(final Long id) {
        final ReservationTime reservationTime = reservationTimeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약 시간이 없습니다."));
        return ReservationTimeResponse.from(reservationTime);
    }

    public void delete(final Long id) {
        final ReservationTime reservationTime = reservationTimeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 예약 시간이 없습니다."));
        validateHasReservation(reservationTime);
        reservationTimeDao.deleteById(reservationTime.getId());
    }

    private void validateHasReservation(final ReservationTime reservationTime) {
        final int reservationCount = reservationDao.countByTimeId(reservationTime.getId());
        if (reservationCount > 0) {
            throw new IllegalArgumentException("해당 예약 시간의 예약 건이 존재합니다.");
        }
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeSearch availableReservationTimeSearch
    ) {
        final List<Long> reservations = reservationDao.findTimeIds(availableReservationTimeSearch);
        final Set<Long> reservedTimeIds = new HashSet<>(reservations);
        final List<ReservationTime> times = reservationTimeDao.findAll();

        return times.stream()
                .map(reservationTime -> {
                    boolean isReserved = reservedTimeIds.contains(reservationTime.getId());
                    return AvailableReservationTimeResponse.of(reservationTime, isReserved);
                })
                .collect(Collectors.toList());
    }
}
