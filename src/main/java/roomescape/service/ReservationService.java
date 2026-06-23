package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Reserver;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationWaitingRepository waitingRepository;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationWaitingRepository waitingRepository,
            ReservationValidator reservationValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationValidator = reservationValidator;
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByReserver(new Reserver(name));
    }

    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByDateRange(startDate, endDate);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation findPendingByUser(Long id, String name, LocalDateTime now) {
        Reservation reservation = findReservation(id);
        reservationValidator.validatePaymentRetryByUser(reservation, name, now);
        return reservation;
    }

    @Transactional
    public Reservation createByUser(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        return createByUser(name, date, timeId, themeId, now, ReservationStatus.CONFIRMED);
    }

    @Transactional
    public Reservation createPendingByUser(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        return createByUser(name, date, timeId, themeId, now, ReservationStatus.PENDING);
    }

    private Reservation createByUser(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now,
                                     ReservationStatus status) {
        ReservationSlot slot = new ReservationSlot(date, findReservationTime(timeId), findTheme(themeId));
        Reservation reservation = new Reservation(null, new Reserver(name), slot, status);
        reservationValidator.validateCreatableByUser(reservation, now);

        return insertReservation(reservation);
    }

    @Transactional
    public Reservation createByAdmin(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationSlot slot = new ReservationSlot(date, findReservationTime(timeId), findTheme(themeId));
        Reservation reservation = new Reservation(null, new Reserver(name), slot);
        reservationValidator.validateCreatableByAdmin(reservation);

        return insertReservation(reservation);
    }

    @Transactional
    public void deleteByUser(Long id, String name, LocalDateTime now) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateDeletableByUser(reservation, name, now);
        deleteAndPromoteWaiting(reservation);
    }

    @Transactional
    public void deleteByAdmin(Long id, LocalDateTime now) {
        Optional<Reservation> foundReservation = reservationRepository.findByIdForUpdate(id);
        if (foundReservation.isEmpty()) {
            return;
        }

        Reservation reservation = foundReservation.get();
        if (reservation.isPast(now)) {
            reservationRepository.delete(reservation.getId());
            return;
        }

        deleteAndPromoteWaiting(reservation);
    }

    @Transactional
    public Reservation updateByUser(Long id, String name, LocalDate updateDate, Long updateTimeId, LocalDateTime now) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateUpdatableByUser(reservation, name, now);

        Reservation updatedReservation = createUpdatedReservation(reservation, updateDate, updateTimeId);
        reservationValidator.validateUpdatedReservation(reservation, updatedReservation, now);

        Optional<ReservationWaiting> firstWaiting = waitingRepository.findFirstBySlotForUpdate(reservation.getSlot());
        updateReservation(updatedReservation);
        firstWaiting.ifPresent(this::promoteWaiting);
        return updatedReservation;
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    private Reservation insertReservation(Reservation reservation) {
        try {
            return reservationRepository.insert(reservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다.");
        }
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

    private Reservation createUpdatedReservation(Reservation reservation, LocalDate updateDate, Long updateTimeId) {
        ReservationSlot originalSlot = reservation.getSlot();
        return new Reservation(
                reservation.getId(),
                reservation.getReserver(),
                new ReservationSlot(
                        resolveUpdateDate(originalSlot.getDate(), updateDate),
                        resolveUpdateTime(originalSlot.getTime(), updateTimeId),
                        originalSlot.getTheme()
                ),
                reservation.getStatus());
    }

    private LocalDate resolveUpdateDate(LocalDate originalDate, LocalDate updateDate) {
        if (updateDate != null) {
            return updateDate;
        }
        return originalDate;
    }

    private ReservationTime resolveUpdateTime(ReservationTime originalTime, Long updateTimeId) {
        if (updateTimeId != null) {
            return findReservationTime(updateTimeId);
        }
        return originalTime;
    }

    private void deleteAndPromoteWaiting(Reservation reservation) {
        Optional<ReservationWaiting> firstWaiting = waitingRepository.findFirstBySlotForUpdate(reservation.getSlot());
        reservationRepository.delete(reservation.getId());
        firstWaiting.ifPresent(this::promoteWaiting);
    }

    private void promoteWaiting(ReservationWaiting firstWaiting) {
        reservationRepository.insert(firstWaiting.promoteToReservation());
        waitingRepository.delete(firstWaiting.getId());
    }

    private void updateReservation(Reservation updatedReservation) {
        try {
            reservationRepository.update(updatedReservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다.");
        }
    }
}
