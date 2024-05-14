package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.AssociatedDataExistsException;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimesResponse;

import java.util.List;

@Service
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(final ReservationTimeDao reservationTimeDao, final ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public ReservationTimesResponse findAllTimes() {
        List<ReservationTimeResponse> response = reservationTimeDao.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();

        return new ReservationTimesResponse(response);
    }

    public ReservationTimeResponse addTime(final ReservationTimeRequest reservationTimeRequest) {
        validateTimeDuplication(reservationTimeRequest);
        ReservationTime reservationTime = reservationTimeDao.insert(reservationTimeRequest.toTime());

        return ReservationTimeResponse.from(reservationTime);
    }

    private void validateTimeDuplication(final ReservationTimeRequest reservationTimeRequest) {
        List<ReservationTime> duplicateReservationTimes = reservationTimeDao.findByStartAt(reservationTimeRequest.startAt());

        if (duplicateReservationTimes.size() > 0) {
            throw new DataDuplicateException(ErrorType.TIME_DUPLICATED,
                    String.format("이미 존재하는 예약 시간(ReservationTime) 입니다. [startAt: %s]", reservationTimeRequest.startAt()));
        }
    }

    public void removeTimeById(final Long id) {
        List<Reservation> usingTimeReservations = reservationDao.findByTimeId(id);
        if (usingTimeReservations.size() > 0) {
            throw new AssociatedDataExistsException(ErrorType.TIME_IS_USED_CONFLICT,
                    String.format("해당 예약 시간(ReservationTime) 에 예약이 존재하여 시간을 삭제할 수 없습니다. [timeId: %d]", id));
        }
        reservationTimeDao.deleteById(id);
    }
}
