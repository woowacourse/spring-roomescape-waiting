package roomescape.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationWaitingDao reservationWaitingDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final ReservationWaitingService waitingService;

    public ReservationService(ReservationDao reservationDao, ReservationWaitingDao reservationWaitingDao,
                              ReservationTimeDao reservationTimeDao, ThemeDao themeDao,
                              ReservationWaitingService waitingService) {
        this.reservationDao = reservationDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.waitingService = waitingService;
    }

    @Transactional
    public ReservationResponse addReservation(CreateReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.date(), reservationTime, theme);

        validateUniqueReservation(slot);
        validatePastDatetime(slot, now);

        Reservation reservation = Reservation.createWithoutId(command.name(), slot);
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
        List<MyReservationResponse> reservationWaitings = reservationWaitingDao.selectByNameWithOrder(name)
                .stream()
                .map(MyReservationResponse::fromReservationWaiting)
                .toList();

        return getMyReservationResponses(reservations, reservationWaitings);
    }

    @Transactional
    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime time = getTime(command.timeId());
        ReservationSlot slot = new ReservationSlot(command.date(), time, reservation.getTheme());

        validateUniqueExcludingSelf(slot, reservationId);
        validatePastDatetime(slot, now);

        Reservation updateReservation = reservationDao.update(reservationId, command.date(), command.timeId());
        return ReservationResponse.from(updateReservation);
    }

    @Transactional
    public void delete(Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservationDao.delete(reservationId);
        waitingService.promoteFirstWaiting(reservation.getSlot());
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.selectById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.selectById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void validateUniqueReservation(ReservationSlot slot) {
        boolean exists = reservationDao.existsBySlot(slot);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validateUniqueExcludingSelf(ReservationSlot slot, long id) {
        boolean exists = reservationDao.existsDuplicateExcluding(slot, id);
        if (exists) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    private void validatePastDatetime(ReservationSlot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_DATETIME);
        }
    }

    private Reservation getReservation(Long reservationId) {
        return reservationDao.selectById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }

    private List<MyReservationResponse> getMyReservationResponses(List<MyReservationResponse> reservations,
                                                                  List<MyReservationResponse> reservationWaitings) {
        List<MyReservationResponse> reservationResponses = new ArrayList<>();
        reservationResponses.addAll(reservations);
        reservationResponses.addAll(reservationWaitings);
        reservationResponses.sort(
                Comparator.comparing(MyReservationResponse::date)
                        .thenComparing(reservation -> reservation.time().startAt())
                        .thenComparing(reservation -> reservation.theme().name())
        );
        return reservationResponses;
    }
}
