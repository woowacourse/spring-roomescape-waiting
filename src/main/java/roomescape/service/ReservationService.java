package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.dao.*;
import roomescape.domain.*;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;

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
    private final ReservationSlotDao reservationSlotDao;

    public ReservationService(ReservationDao reservationDao, ReservationWaitingDao reservationWaitingDao, ReservationTimeDao reservationTimeDao,
                              ThemeDao themeDao, ReservationSlotDao reservationSlotDao) {
        this.reservationDao = reservationDao;
        this.reservationWaitingDao = reservationWaitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.reservationSlotDao = reservationSlotDao;
    }

    @Transactional
    public ReservationResponse addReservation(CreateReservationCommand command, LocalDateTime now) {
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.date(), reservationTime, theme);
        ReservationSlot savedSlot = reservationSlotDao.findOrCreate(slot);

        savedSlot.validateNotPast(now);
        validateUniqueReservation(savedSlot);

        Reservation reservation = Reservation.createWithoutId(command.name(), savedSlot);

        try {
            Reservation savedReservation = reservationDao.insert(reservation);
            return ReservationResponse.from(savedReservation);
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
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
                .map(waiting -> {
                    ReservationSlot slot = waiting.getSlot();

                    ReservationWaitingQueue waitings = new ReservationWaitingQueue(reservationWaitingDao.selectBySlot(slot));
                    int order = waitings.orderOf(waiting);

                    return MyReservationResponse.fromReservationWaiting(waiting, order);
                })
                .toList();

        List<MyReservationResponse> reservationResponses = new ArrayList<>();
        reservationResponses.addAll(reservations);
        reservationResponses.addAll(reservationWaitings);
        reservationResponses.sort(Comparator.comparing(MyReservationResponse::date));

        return reservationResponses;
    }

    @Transactional
    public ReservationResponse update(Long reservationId, UpdateReservationCommand command, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        ReservationTime time = getTime(command.timeId());
        Theme theme = reservation.getTheme();
        ReservationSlot slot = new ReservationSlot(command.date(), time, theme);
        ReservationSlot savedSlot = reservationSlotDao.findOrCreate(slot);

        savedSlot.validateNotPast(now);
        validateUniqueExcludingSelf(savedSlot, reservationId);

        try {
            Reservation updateReservation = reservationDao.update(reservationId, savedSlot);
            return ReservationResponse.from(updateReservation);
        } catch (DuplicateKeyException exception) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE);
        }
    }

    @Transactional
    public void delete(Long reservationId) {
        int deleted = reservationDao.delete(reservationId);
        if (deleted == 0) {
            throw new RoomEscapeException(ReservationErrorCode.NOT_FOUND);
        }
    }

    @Transactional
    public void cancel(Long reservationId, LocalDateTime now) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateCancelable(now);

        ReservationWaitingQueue waitings = new ReservationWaitingQueue(
                reservationWaitingDao.selectBySlot(reservation.getSlot())
        );

        delete(reservationId);

        waitings.first()
                .ifPresent(this::promoteWaitingToReservation);
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
        boolean exists = reservationDao.existsByDateAndTimeIdAndThemeId(slot);
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

    private Reservation getReservation(Long reservationId) {
        return reservationDao.selectById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));
    }

    private void promoteWaitingToReservation(ReservationWaiting waiting) {
        Reservation promotedReservation = waiting.promoteToReservation();

        reservationDao.insert(promotedReservation);
        reservationWaitingDao.delete(waiting.getId());
    }
}
