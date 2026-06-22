package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationWaitingRepository reservationWaitingRepository;
    private final JpaThemeRepository themeRepository;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            JpaReservationRepository reservationRepository,
            JpaReservationTimeRepository reservationTimeRepository,
            JpaReservationWaitingRepository reservationWaitingRepository,
            JpaThemeRepository themeRepository,
            ReservationValidator reservationValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.themeRepository = themeRepository;
        this.reservationValidator = reservationValidator;
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        reservationValidator.validateNotPast(date, time);
        return createReservation(name, date, timeId, themeId, time);
    }

    @Transactional
    public Reservation createByAdmin(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        return createReservation(name, date, timeId, themeId, time);
    }

    @Transactional
    public void delete(Long id, String name) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateUpdatableReservation(reservation, name);
        ReservationSlot deletedSlot = ReservationSlot.from(reservation);

        reservationRepository.deleteById(id);
        reservationRepository.flush();
        promoteFirstWaiting(deletedSlot);
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        Reservation reservation = findReservation(id);
        ReservationSlot deletedSlot = ReservationSlot.from(reservation);

        reservationRepository.deleteById(id);
        if (!reservation.isPast()) {
            reservationRepository.flush();
            promoteFirstWaiting(deletedSlot);
        }
    }

    @Transactional
    public Reservation update(Long id, String name, LocalDate date, Long timeId) {
        Reservation reservation = findReservation(id);
        ReservationSlot previousSlot = ReservationSlot.from(reservation);

        reservationValidator.validateUpdatableReservation(reservation, name);

        Reservation updatedReservation = createUpdatedReservation(reservation, date, timeId);
        reservationValidator.validateUpdatePolicy(reservation, updatedReservation);

        reservation.changeSchedule(updatedReservation.getTime(), updatedReservation.getDate());

        try {
            reservationRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다.");
        }
        promoteFirstWaiting(previousSlot);
        return reservation;
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다.");
        }
    }

    private void promoteFirstWaiting(ReservationSlot slot) {
        try {
            reservationWaitingRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(
                            slot.date(),
                            slot.timeId(),
                            slot.themeId())
                    .ifPresent(this::promote);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.RESERVATION_OPERATION_CONFLICT);
        }
    }

    private void promote(ReservationWaiting waiting) {
        Reservation reservation = new Reservation(
                null,
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme());

        reservationRepository.save(reservation);
        reservationWaitingRepository.delete(waiting);
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
    }

    private Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId,
                                          ReservationTime time) {
        reservationValidator.validateNotReserved(date, timeId, themeId);
        Theme theme = findTheme(themeId);

        Reservation reservation = new Reservation(null, name, date, time, theme);
        return save(reservation);
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    private Reservation createUpdatedReservation(Reservation reservation, LocalDate date, Long timeId) {
        reservationValidator.validateUpdateValueExists(date, timeId);

        return new Reservation(
                reservation.getId(),
                reservation.getName(),
                resolveUpdateDate(reservation, date),
                resolveUpdateTime(reservation, timeId),
                reservation.getTheme());
    }

    private LocalDate resolveUpdateDate(Reservation reservation, LocalDate date) {
        if (date != null) {
            return date;
        }
        return reservation.getDate();
    }

    private ReservationTime resolveUpdateTime(Reservation reservation, Long timeId) {
        if (timeId != null) {
            return findReservationTime(timeId);
        }
        return reservation.getTime();
    }
}
