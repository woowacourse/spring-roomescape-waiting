package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.dao.dto.WaitingQueryResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.infrastructure.SlotManager;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationDetailResults;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.WaitingDetailResult;
import roomescape.service.event.ReservationChangeEvent;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final WaitingDao waitingDao;
    private final ReservationRejectLogger reservationRejectLogger;
    private final SlotManager slotManager;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public ReservationService(
            ReservationDao reservationDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao,
            WaitingDao waitingDao,
            ReservationRejectLogger reservationRejectLogger,
            SlotManager slotManager,
            ApplicationEventPublisher eventPublisher,
            Clock clock
    ) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.waitingDao = waitingDao;
        this.reservationRejectLogger = reservationRejectLogger;
        this.slotManager = slotManager;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public List<ReservationResult> findReservations() {
        List<Reservation> reservations = reservationDao.findAll();

        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationDetailResults findReservationsByUserName(String userName) {
        List<Reservation> reservations = reservationDao.findByUserName(userName);
        List<WaitingQueryResult> waitings = waitingDao.findByUserName(userName);

        return new ReservationDetailResults(
                reservations.stream()
                        .map(ReservationResult::from)
                        .toList(),
                waitings.stream()
                        .map(WaitingDetailResult::from)
                        .toList()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ReservationResult registerReservation(ReservationCommand command) {
        ReservationTime time = reservationTimeDao.findById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Theme theme = themeDao.findThemeById(command.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        Reservation pending = Reservation.createPending(UserName.parse(command.name()), command.date(), time, theme);

        pending.verifyBookable(LocalDateTime.now(clock));

        EventSlot eventSlot = pending.getEventSlot();
        if (!slotManager.tryAcquire(eventSlot)) {
            Reservation rejected = pending.reject();
            reservationRejectLogger.logRejection(rejected);
            throw new ConflictException("다른 사용자가 예약했습니다. 다시 시도해주세요.");
        }

        Reservation saved = reservationDao.save(pending);

        return ReservationResult.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReservationResult confirmReservation(Long id) {
        Reservation pending = reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));

        Reservation confirmed = pending.confirm();
        reservationDao.update(confirmed);

        return ReservationResult.from(confirmed);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReservationResult changeDateTime(Long id, ReservationCommand command) {
        Reservation origin = reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("변경하려는 예약이 존재하지 않습니다."));

        ReservationTime newTime = reservationTimeDao.findById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        EventSlot originEventSlot = origin.getEventSlot();
        EventSlot modifiedEventSlot = EventSlot.from(command.date(), newTime, origin.getEventSlot().theme());

        if (originEventSlot.equals(modifiedEventSlot)) {
            return ReservationResult.from(origin);
        }

        if (!slotManager.tryAcquire(modifiedEventSlot)) {
            throw new ConflictException("다른 사용자가 예약했습니다. 다시 시도해주세요.");
        }

        Reservation modified = origin.change(
                UserName.parse(command.name()),
                command.date(),
                newTime,
                LocalDateTime.now(clock)
        );

        boolean isSuccessful = reservationDao.update(modified);

        if (!isSuccessful) {
            slotManager.release(modifiedEventSlot);
            throw new NotFoundException("예약 변경 중 문제가 발생했습니다. (이미 취소된 예약일 수 있습니다.)");
        }

        eventPublisher.publishEvent(new ReservationChangeEvent(originEventSlot));

        return ReservationResult.from(modified);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservation(Long id) {
        Reservation origin = reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약이 존재하지 않습니다."));

        Reservation canceled = origin.cancel();

        reservationDao.update(canceled);

        eventPublisher.publishEvent(new ReservationChangeEvent(origin.getEventSlot()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservation(Long id, String userName) {
        Reservation origin = reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약이 존재하지 않습니다."));

        Reservation canceled = origin.cancel(UserName.parse(userName), LocalDateTime.now(clock));

        reservationDao.update(canceled);

        eventPublisher.publishEvent(new ReservationChangeEvent(origin.getEventSlot()));
    }
}
