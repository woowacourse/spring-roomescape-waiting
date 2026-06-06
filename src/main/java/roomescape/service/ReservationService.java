package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import jakarta.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;
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
import roomescape.repository.exception.ReservationSlotAlreadyOccupiedException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            ReservationRepository reservationRepository, WaitlistRepository waitlistRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<ReservationWithStatus> getMyReservations(String name) {
        return reservationRepository.findByName(name);
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
                getReservationTime(request.timeId()),
                getTheme(request.themeId()));

        reservation.verifyReservable(LocalDateTime.now());
        verifyNoDuplicateReservation(reservation);

        try {
            Reservation saved = getReservation(reservationRepository.save(reservation));
            return ReservationWithStatus.reserved(saved);
        } catch (ReservationSlotAlreadyOccupiedException e) {
            return registerWaitlist(reservation);
        }
    }

    private ReservationWithStatus registerWaitlist(Reservation reservation) {
        Long savedId = waitlistRepository.save(reservation);
        Waitlist waitlist = getWaitlist(savedId);
        int waitOrder = waitlistRepository.countBefore(waitlist) + 1;
        return ReservationWithStatus.waiting(waitlist, waitOrder);
    }

    private void verifyNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 예약이 존재합니다.");
        }
        if (waitlistRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "같은 슬롯에 중복 대기가 존재합니다.");
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return timeRepository.getById(timeId, "예약할 수 없는 시간입니다.");
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
    public void cancelMyReservation(Long id, String name) {
        Reservation reservation = getReservation(id);
        reservation.verifyCancelableBy(name, LocalDateTime.now());
        reservationRepository.deleteById(id);

        promoteFirstWaitlistToReservation(reservation);
    }

    private void promoteFirstWaitlistToReservation(Reservation reservation) {
        waitlistRepository.findFirstWaitlistByReservationSlot(reservation)
                .ifPresent(firstWaitlist -> {
                    reservationRepository.save(firstWaitlist.toReservation());
                    waitlistRepository.deleteById(firstWaitlist.getId());
                });
    }

    @Transactional
    public Reservation updateReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation original = getReservation(id);
        Reservation updated = original.changeBy(
                name,
                LocalDateTime.now(),
                request.date(),
                getReservationTime(request.timeId())
        );
        verifyNoConflict(updated);
        reservationRepository.updateDateTime(updated);

        promoteFirstWaitlistToReservation(original);
        return getReservation(id);
    }

    private void verifyNoConflict(Reservation reservation) {
        if (reservationRepository.existsBy(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 시점·테마에 예약이 존재합니다.");
        }
    }
}
