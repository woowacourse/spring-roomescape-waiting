package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.InvalidCreateArgumentException;
import roomescape.exception.business.NotFoundException;
import roomescape.exception.business.RelatedEntityExistException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;
import static roomescape.exception.ErrorCode.RESERVATION_TIME_ALREADY_EXIST;
import static roomescape.exception.ErrorCode.RESERVATION_TIME_INTERVAL_INVALID;
import static roomescape.exception.ErrorCode.RESERVED_RESERVATION_TIME;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimes reservationTimes;
    private final Reservations reservations;

    @Transactional
    public ReservationTimeDto addAndGet(final LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        validateNoDuplication(reservationTime);
        validateTimeInterval(reservationTime);

        reservationTimes.save(reservationTime);
        return ReservationTimeDto.fromEntity(reservationTime);
    }

    private void validateNoDuplication(final ReservationTime reservationTime) {
        boolean isExist = reservationTimes.existByTime(reservationTime.startTimeValue());
        if (isExist) {
            throw new DuplicatedException(RESERVATION_TIME_ALREADY_EXIST);
        }
    }

    private void validateTimeInterval(final ReservationTime reservationTime) {
        boolean existInInterval = reservationTimes.existBetween(reservationTime.startInterval(), reservationTime.endInterval());
        if (existInInterval) {
            throw new InvalidCreateArgumentException(RESERVATION_TIME_INTERVAL_INVALID);
        }
    }

    public List<ReservationTimeDto> getAll() {
        List<ReservationTime> reservationTimes = this.reservationTimes.findAll();
        return ReservationTimeDto.fromEntities(reservationTimes);
    }

    public List<ReservableReservationTimeDto> getAllByDateAndThemeId(final LocalDate date, final String themeIdValue) {
        Id themeId = Id.create(themeIdValue);
        List<ReservationTime> available = reservationTimes.findAvailableByDateAndThemeId(date, themeId);
        List<ReservationTime> notAvailable = reservationTimes.findNotAvailableByDateAndThemeId(date, themeId);

        return ReservableReservationTimeDto.fromEntities(available, notAvailable);
    }

    @Transactional
    public void delete(final String timeIdValue) {
        Id timeId = Id.create(timeIdValue);
        if (reservations.existByTimeId(timeId)) {
            throw new RelatedEntityExistException(RESERVED_RESERVATION_TIME);
        }
        if (!reservationTimes.existById(timeId)) {
            throw new NotFoundException(RESERVATION_NOT_EXIST);
        }
        reservationTimes.deleteById(timeId);
    }
}
