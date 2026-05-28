package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(ReservationDao reservationDao, ReservationWaitingDao reservationWaitingDao, ReservationTimeDao reservationTimeDao,
                              ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    @Transactional
    public ReservationResponse addReservation(CreateReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());

        validateUniqueReservation(command.date(), command.timeId(), command.themeId());
        validatePastDatetime(command.date(), now, reservationTime);

        Reservation reservation = Reservation.createWithoutId(command.name(), command.date(), reservationTime, theme);
        Reservation savedReservation = reservationDao.insert(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationDao.select().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> getMyReservations(String name) {
        List<MyReservationResponse> reservations = reservationDao.selectByName(name).stream()
                .map(MyReservationResponse::fromReservation)
                .toList();
        List<MyReservationResponse> reservationWaitings = reservationWaitingDao.selectByName(name).stream()
                .map(waiting -> MyReservationResponse.fromReservationWaiting(
                        waiting,
                        reservationWaitingDao.countOrder(
                                waiting.getReservationDate(),
                                waiting.getTime().getId(),
                                waiting.getTheme().getId(),
                                waiting.getId()
                        )))
                .toList();

        List<MyReservationResponse> reservationResponses = new ArrayList<>();
        reservationResponses.addAll(reservations);
        reservationResponses.addAll(reservationWaitings);
        reservationResponses.sort(Comparator.comparing(MyReservationResponse::date));

        return reservationResponses;
    }

    @Transactional
    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        getReservation(reservationId);

        ReservationTime time = getTime(command.timeId());
        validateUniqueExcludingSelf(command.date(), command.timeId(),
                getReservation(reservationId).getTheme().getId(), reservationId);
        validatePastDatetime(command.date(), now, time);

        Reservation updateReservation = reservationDao.update(reservationId, command.date(), command.timeId());
        return ReservationResponse.from(updateReservation);
    }

    @Transactional
    public void delete(Long reservationId) {
        int deleted = reservationDao.delete(reservationId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationErrorCode.NOT_FOUND);
        }
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.selectById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateUniqueReservation(LocalDate date, long timeId, long themeId) {
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueExcludingSelf(LocalDate date, long timeId, long themeId, long id) {
        boolean exists = reservationDao.existsDuplicateExcluding(date, timeId, themeId, id);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(LocalDate date, LocalDateTime now, ReservationTime reservationTime) {
        if (reservationTime.isPast(date, now)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.PAST_DATETIME);
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationDao.selectById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }
}
