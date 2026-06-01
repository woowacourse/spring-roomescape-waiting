package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.*;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ReservationDao;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final int EMPTY_RESERVATION_COUNT = 0;
    private final ReservationDao reservationDao;
    private final ScheduleService scheduleService;

    public ReservationService(ReservationDao reservationDao, ScheduleService scheduleService) {
        this.reservationDao = reservationDao;
        this.scheduleService = scheduleService;
    }

    @Transactional
    public Long saveReservation(ReservationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Reserver reserver = new Reserver(request.name());
        Schedule schedule = scheduleService.getOrCreateSchedule(request.date(), request.timeId(), request.themeId());

        if (reservationDao.existByNameAndScheduleId(reserver.getName(), schedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "이미 해당 스케줄에 본인의 예약이 존재합니다.");
        }

        Reservation reservation = Reservation.createBy(
                reserver,
                schedule,
                calculateReservationStatus(schedule.getId()),
                now
        );

        return reservationDao.save(reservation);
    }

    @Transactional
    public void updateReservation(long reservationId, ReservationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Reserver reserver = new Reserver(request.name());
        Schedule targetSchedule = scheduleService.getOrCreateSchedule(request.date(), request.timeId(), request.themeId());

        if (reservationDao.existByNameAndScheduleId(reserver.getName(), targetSchedule.getId())) {
            throw new RoomescapeException(DomainErrorCode.DUPLICATE_RESERVATION, "변경하려는 스케줄에 본인의 예약이 존재합니다.");
        }

        Reservation previous = getById(reservationId);
        Reservation updated = previous.updateBy(
                reserver,
                targetSchedule,
                calculateReservationStatus(targetSchedule.getId()),
                now
        );

        reservationDao.update(updated);
        promoteWaitingReservation(previous, previous.getSchedule().getId());
    }

    @Transactional
    public void cancelReservation(long reservationId, String name) {
        LocalDateTime now = LocalDateTime.now();
        Reserver reserver = new Reserver(name);
        Reservation reservation = getById(reservationId);

        if (reservation.isAlreadyCanceled()) {
            return;
        }

        Schedule schedule = scheduleService.getById(reservation.getSchedule().getId());
        Reservation changed = reservation.changeBy(
                reserver,
                schedule,
                now
        );

        reservationDao.changeStatusWithUpdateAt(changed);
        promoteWaitingReservation(reservation, schedule.getId());
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll()
                .stream()
                .map(this::toReservationResponse)
                .toList();
    }

    public List<ReservationResponse> findByName(String name) {
        return reservationDao.findByName(name)
                .stream()
                .map(this::toReservationResponse)
                .toList();
    }

    private ReservationResponse toReservationResponse(Reservation reservation) {
        return ReservationResponse.from(
                reservation,
                reservationDao.findOrderByReservationId(reservation.getId()),
                reservation.getUpdateAt()
        );
    }

    private void promoteWaitingReservation(Reservation changed, long scheduleId) {
        if (changed.isReserved()) {
            Optional<Reservation> reservations = reservationDao.findFirstByScheduleIdAndStatus(scheduleId, ReservationStatus.WAITING);
            reservations.ifPresent(reservation
                    -> reservationDao.changeStatusOnly(reservation.getId(), ReservationStatus.RESERVED)
            );
        }
    }

    private ReservationStatus calculateReservationStatus(long scheduleId) {
        if (countReservationBySchedule(scheduleId) == EMPTY_RESERVATION_COUNT) {
            return ReservationStatus.RESERVED;
        }

        return ReservationStatus.WAITING;
    }

    private int countReservationBySchedule(long scheduleId) {
        return reservationDao.countReservationByScheduleId(scheduleId);
    }

    private Reservation getById(long id) {
        return reservationDao.findById(id).orElseThrow(()
                -> new RoomescapeException(DomainErrorCode.NOT_FOUND_RESERVATION, "해당 ID의 예약이 존재하지 않습니다. ID: " + id)
        );
    }
}
