package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final EntityManager entityManager;

    public ReservationService(
            ReservationRepository reservationRepository, WaitlistRepository waitlistRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository,
            EntityManager entityManager
    ) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.entityManager = entityManager;
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAllByOrderByDateDescTime_StartAtAsc();
    }

    public List<ReservationWithStatus> getMyReservations(String name) {
        List<ReservationWithStatus> reservations = reservationRepository.findByName(name)
                .stream()
                .map(ReservationWithStatus::reserved)
                .toList();
        List<ReservationWithStatus> waitlists = waitlistRepository.findByName(name)
                .stream()
                .map(waitlist -> ReservationWithStatus.waiting(
                        waitlist,
                        waitlistRepository.countBefore(
                                waitlist.getDate(),
                                waitlist.getTime().getId(),
                                waitlist.getTheme().getId(),
                                waitlist.getCreatedAt(),
                                waitlist.getId()) + 1
                ))
                .toList();

        return Stream.concat(reservations.stream(), waitlists.stream())
                .sorted(Comparator
                        .comparing(ReservationWithStatus::getDate).reversed()
                        .thenComparing(reservation -> reservation.getTime().getStartAt()))
                .toList();
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.getById(id, "존재하지 않는 예약입니다.");
    }

    public Waitlist getWaitlist(Long id) {
        return waitlistRepository.getById(id, "존재하지 않는 예약 대기입니다.");
    }

    @Transactional
    public ReservationWithStatus applyReservation(ReservationRequest request) {
        Reservation reservation = createReservation(
                request,
                getReservationTimeForUpdate(request.timeId()),
                getTheme(request.themeId()));

        reservation.verifyReservable(LocalDateTime.now());
        verifyNoDuplicateReservation(reservation);

        if (reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            return registerWaitlist(reservation);
        }

        try {
            return ReservationWithStatus.reserved(reservationRepository.saveAndFlush(reservation));
        } catch (DataIntegrityViolationException e) {
            if (isReservationSlotUniqueViolation(e)) {
                entityManager.clear();
                return registerWaitlist(reservation);
            }
            throw e;
        }
    }

    private boolean isReservationSlotUniqueViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();

        return message != null && message.contains("UK_RESERVATION_SLOT");
    }

    private ReservationWithStatus registerWaitlist(Reservation reservation) {
        Long savedId = waitlistRepository.save(new Waitlist(
                reservation.getName(),
                reservation.getDate(),
                LocalDateTime.now(),
                reservation.getTime(),
                reservation.getTheme()
        )).getId();
        Waitlist waitlist = getWaitlist(savedId);
        int waitOrder = waitlistRepository.countBefore(
                waitlist.getDate(),
                waitlist.getTime().getId(),
                waitlist.getTheme().getId(),
                waitlist.getCreatedAt(),
                waitlist.getId()) + 1;
        return ReservationWithStatus.waiting(waitlist, waitOrder);
    }

    private void verifyNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 예약이 존재합니다.");
        }
        if (waitlistRepository.existsByNameAndDateAndTime_IdAndTheme_Id(
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        )) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "같은 슬롯에 중복 대기가 존재합니다.");
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return timeRepository.getById(timeId, "예약할 수 없는 시간입니다.");
    }

    private ReservationTime getReservationTimeForUpdate(Long timeId) {
        return timeRepository.getByIdForUpdate(timeId, "예약할 수 없는 시간입니다.");
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.getById(themeId, "예약할 수 없는 테마입니다.");
    }

    @Nonnull
    private Reservation createReservation(ReservationRequest request, ReservationTime reservationTime,
                                          Theme theme) {
        return new Reservation(
                request.name(),
                request.date(),
                reservationTime,
                theme
        );
    }

    @Transactional
    public void deleteReservation(Long id) {
        getReservation(id);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void cancelMyReservationAndPromoteWaitlist(Long id, String name) {
        Reservation reservation = getReservation(id);
        reservation.verifyCancelableBy(name, LocalDateTime.now());
        reservationRepository.deleteById(id);
        reservationRepository.flush();

        promoteFirstWaitlistToReservation(reservation);
    }

    private void promoteFirstWaitlistToReservation(Reservation reservation) {
        waitlistRepository.findFirstByDateAndTime_IdAndTheme_IdOrderByCreatedAtAscIdAsc(reservation.getDate(),
                        reservation.getTime().getId(), reservation.getTheme().getId())
                .ifPresent(firstWaitlist -> {
                    reservationRepository.save(firstWaitlist.toReservation());
                    waitlistRepository.deleteById(firstWaitlist.getId());
                });
    }

    @Transactional
    public Reservation updateMyReservationAndPromoteWaitlist(Long id, String name, ReservationUpdateRequest request) {
        Reservation original = getReservation(id);
        Reservation originalSlot = new Reservation(original.getName(), original.getDate(), original.getTime(),
                original.getTheme());
        updateMyReservation(name, request, original);

        promoteFirstWaitlistToReservation(originalSlot);
        return getReservation(id);
    }

    private void updateMyReservation(String name, ReservationUpdateRequest request, Reservation original) {
        try {
            ReservationTime newTime = getReservationTimeForUpdate(request.timeId());
            if (isChangingToOccupiedSlot(original, request.date(), newTime)) {
                throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 시점·테마에 예약이 존재합니다.");
            }
            original.changeDateAndTime(
                    name,
                    LocalDateTime.now(),
                    request.date(),
                    newTime
            );
            reservationRepository.flush();
        } catch (DataIntegrityViolationException e) {
            if (isReservationSlotUniqueViolation(e)) {
                throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 시점·테마에 예약이 존재합니다.");
            }
            throw e;
        }
    }

    private boolean isChangingToOccupiedSlot(Reservation original, LocalDate newDate, ReservationTime newTime) {
        if (original.getDate().equals(newDate) && original.getTime().getId().equals(newTime.getId())) {
            return false;
        }
        return reservationRepository.existsByDateAndTime_IdAndTheme_Id(
                newDate,
                newTime.getId(),
                original.getTheme().getId()
        );
    }

}
