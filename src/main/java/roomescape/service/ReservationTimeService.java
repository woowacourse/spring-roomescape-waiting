package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.dto.AvailableReservationTimeResult;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.ExistReservationException;
import roomescape.exception.NotExistException;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.service.dto.output.ReservationTimeOutput;

import static roomescape.exception.ExceptionDomainType.RESERVATION_TIME;


@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(final ReservationTimeDao reservationTimeDao, final ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public ReservationTimeOutput createReservationTime(final ReservationTimeInput input) {
        final ReservationTime reservationTime = input.toReservationTime();

        if (reservationTimeDao.isExistByStartAt(reservationTime.getStartAtAsString())) {
            throw new AlreadyExistsException(RESERVATION_TIME, reservationTime.getStartAtAsString());
        }

        final ReservationTime savedReservationTime = reservationTimeDao.create(reservationTime);
        return ReservationTimeOutput.toOutput(savedReservationTime);
    }

    public List<ReservationTimeOutput> getAllReservationTimes() {
        final List<ReservationTime> reservationTimes = reservationTimeDao.getAll();
        return ReservationTimeOutput.toOutputs(reservationTimes);
    }

    public List<AvailableReservationTimeOutput> getAvailableTimes(final AvailableReservationTimeInput input) {
        final List<AvailableReservationTimeResult> availableReservationTimeResults =
                reservationTimeDao.getAvailable(new ReservationDate(input.date()), input.themeId());
        return AvailableReservationTimeOutput.toOutputs(availableReservationTimeResults);
    }

    public void deleteReservationTime(final long id) {
        if (reservationDao.isExistByTimeId(id)) {
            throw new ExistReservationException(RESERVATION_TIME, id);
        }
        if (reservationTimeDao.delete(id)) {
            return;
        }
        throw new NotExistException(RESERVATION_TIME, id);
    }
}
