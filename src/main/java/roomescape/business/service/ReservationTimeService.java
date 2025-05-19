package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
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

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationTimeDto addAndGet(final LocalTime time) {
        ReservationTime reservationTime = ReservationTime.create(time);
        validateNoDuplication(reservationTime);
        validateTimeInterval(reservationTime);

        reservationTimeRepository.save(reservationTime);
        return ReservationTimeDto.fromEntity(reservationTime);
    }

    private void validateNoDuplication(final ReservationTime reservationTime) {
        boolean isExist = reservationTimeRepository.existByTime(reservationTime.startTimeValue());
        if (isExist) {
            throw new DuplicatedException(RESERVATION_TIME_ALREADY_EXIST);
        }
    }

    private void validateTimeInterval(final ReservationTime reservationTime) {
        boolean existInInterval = reservationTimeRepository.existBetween(reservationTime.startInterval(), reservationTime.endInterval());
        if (existInInterval) {
            throw new InvalidCreateArgumentException(RESERVATION_TIME_INTERVAL_INVALID);
        }
    }

    public List<ReservationTimeDto> getAll() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return ReservationTimeDto.fromEntities(reservationTimes);
    }

    public List<ReservableReservationTimeDto> getAllByDateAndThemeId(final LocalDate date, final String themeIdValue) {
        Id themeId = Id.create(themeIdValue);
        final List<ReservationTime> available = reservationTimeRepository.findAvailableByDateAndThemeId(date, themeId);
        final List<ReservationTime> notAvailable = reservationTimeRepository.findNotAvailableByDateAndThemeId(date, themeId);

        return ReservableReservationTimeDto.fromEntities(available, notAvailable);
    }

    @Transactional
    public void delete(final String timeIdValue) {
        Id timeId = Id.create(timeIdValue);
        if (reservationRepository.existByTimeId(timeId)) {
            throw new RelatedEntityExistException(RESERVED_RESERVATION_TIME);
        }
        if (!reservationTimeRepository.existById(timeId)) {
            throw new NotFoundException(RESERVATION_NOT_EXIST);
        }
        reservationTimeRepository.deleteById(timeId);
    }
}
