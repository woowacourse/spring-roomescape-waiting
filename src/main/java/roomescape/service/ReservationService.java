package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import jakarta.annotation.Nonnull;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
    private final Clock clock;
    private final ReservationWriter reservationWriter;
    private final WaitlistWriter waitlistWriter;
    private final WaitlistOrderPolicy waitlistOrderPolicy;

    public ReservationService(
            ReservationRepository reservationRepository, WaitlistRepository waitlistRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository,
            Clock clock,
            ReservationWriter reservationWriter,
            WaitlistWriter waitlistWriter,
            WaitlistOrderPolicy waitlistOrderPolicy
    ) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
        this.reservationWriter = reservationWriter;
        this.waitlistWriter = waitlistWriter;
        this.waitlistOrderPolicy = waitlistOrderPolicy;
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<ReservationWithStatus> getMyReservations(String name) {
        List<ReservationWithStatus> results = new ArrayList<>();

        for (Reservation reservation : reservationRepository.findByName(name)) {
            results.add(ReservationWithStatus.reserved(reservation));
        }

        for (Waitlist waitlist : waitlistRepository.findByName(name)) {
            results.add(ReservationWithStatus.waiting(waitlist, calculateWaitingOrder(waitlist)));
        }

        results.sort(Comparator.comparing(ReservationWithStatus::getDate).reversed()
                .thenComparing(reservation -> reservation.getTime().getStartAt()));

        return results;
    }

    private int calculateWaitingOrder(Waitlist waitlist) {
        List<Waitlist> sameSlotWaitlists = waitlistRepository.findBySlot(
                waitlist.getDate(),
                waitlist.getTime().getId(),
                waitlist.getTheme().getId()
        );

        return waitlistOrderPolicy.calculateOrder(waitlist, sameSlotWaitlists);
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.getById(id, "존재하지 않는 예약입니다.");
    }

    public Waitlist getWaitlist(Long id) {
        return waitlistRepository.getById(id, "존재하지 않는 예약 대기입니다.");
    }

    @Transactional
    public ReservationWithStatus reserveOrWait(ReservationRequest request) {
        Reservation reservation = createReservation(
                request,
                getReservationTime(request.timeId()),
                getTheme(request.themeId()));

        reservation.verifyReservable(LocalDateTime.now(clock));

        try {
            Reservation saved = reservationWriter.save(reservation);
            return ReservationWithStatus.reserved(saved);
        } catch (DuplicateKeyException e) {
            return waitlistWriter.save(reservation);
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
        reservation.verifyCancelableBy(name, LocalDateTime.now(clock));
        reservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation updateReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation original = getReservation(id);
        Reservation updated = original.changeBy(
                name,
                LocalDateTime.now(clock),
                request.date(),
                getReservationTime(request.timeId())
        );
        verifyNoConflict(updated);
        reservationRepository.updateDateTime(updated);
        return getReservation(id);
    }

    private void verifyNoConflict(Reservation reservation) {
        if (reservationRepository.existsBy(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 시점·테마에 예약이 존재합니다.");
        }
    }
}
