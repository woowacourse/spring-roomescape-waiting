package roomescape.reservationTime.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForeignKeyException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.ReservationTimeExceptionMessage;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservationTime.dao.ReservationTimeDao;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.dto.admin.ReservationTimeRequest;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.reservationTime.dto.user.AvailableReservationTimeRequest;
import roomescape.reservationTime.dto.user.AvailableReservationTimeResponse;
import roomescape.theme.dao.ThemeDao;

@Service
public class ReservationTimeService {

    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;
    private final ThemeDao themeDao;

    public ReservationTimeService(
            ReservationTimeDao reservationTimeDao,
            ReservationDao reservationDao,
            ThemeDao themeDao
    ) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
        this.themeDao = themeDao;
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeDao.findAll().stream()
                .map(reservationTime -> new ReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt())
                )
                .toList();
    }

    public List<AvailableReservationTimeResponse> findByDateAndTheme(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        searchTheme(availableReservationTimeRequest);
        List<ReservationTime> reservedTimes = findReservedTimes(availableReservationTimeRequest);
        Set<Long> reservedIds = findReservedTimeIds(reservedTimes);

        List<ReservationTime> availableReservationTimes = reservationTimeDao.findAll();
        return availableReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponse(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        reservedIds.contains(reservationTime.getId())
                ))
                .toList();
    }

    private void searchTheme(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        themeDao.findById(availableReservationTimeRequest.themeId())
                .orElseThrow(() -> new InvalidIdException(
                        IdExceptionMessage.INVALID_THEME_ID.getMessage()
                ));
    }

    private List<ReservationTime> findReservedTimes(
            final AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        return reservationDao.findAll().stream()
                .filter(reservation -> reservation.getDate().equals(availableReservationTimeRequest.date())
                        && reservation.getThemeId().equals(availableReservationTimeRequest.themeId())
                ).map(reservation -> new ReservationTime(
                        reservation.getTimeId(),
                        reservation.getTimeStartAt())
                )
                .toList();
    }

    private Set<Long> findReservedTimeIds(final List<ReservationTime> reservedTimes) {
        return reservedTimes.stream()
                .map(ReservationTime::getId)
                .collect(Collectors.toSet());
    }

    public ReservationTimeResponse add(final ReservationTimeRequest reservationTimeRequest) {
        validateDuplicate(reservationTimeRequest);

        ReservationTime newReservationTime = new ReservationTime(reservationTimeRequest.startAt());
        ReservationTime savedReservationTime = reservationTimeDao.add(newReservationTime);
        return new ReservationTimeResponse(savedReservationTime.getId(), savedReservationTime.getStartAt());
    }

    private void validateDuplicate(final ReservationTimeRequest reservationTimeRequest) {
        boolean isDuplicate = reservationTimeDao.existsByStartAt(reservationTimeRequest.startAt());

        if (isDuplicate) {
            throw new DuplicateException(ReservationTimeExceptionMessage.DUPLICATE_TIME.getMessage());
        }
    }

    public void deleteById(final Long id) {
        searchReservationTimeId(id);
        validateUnoccupiedTime(id);
        reservationTimeDao.deleteById(id);
    }

    private void searchReservationTimeId(final Long id) {
        reservationTimeDao.findById(id)
                .orElseThrow(
                        () -> new InvalidIdException(IdExceptionMessage.INVALID_TIME_ID.getMessage()));
    }

    private void validateUnoccupiedTime(final Long id) {
        boolean isOccupiedTimeId = reservationTimeDao.existsByReservationTimeId(id);

        if (isOccupiedTimeId) {
            throw new ForeignKeyException(ReservationTimeExceptionMessage.RESERVED_TIME.getMessage());
        }
    }
}
