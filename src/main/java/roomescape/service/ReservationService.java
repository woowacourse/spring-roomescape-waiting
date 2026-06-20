package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.RESERVATION_TIME_NOT_FOUND;
import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final SlotRepository slotRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;
    private final ReservationWriter reservationWriter;
    private final WaitlistWriter waitlistWriter;
    private final WaitlistOrderPolicy waitlistOrderPolicy;

    public ReservationService(
        ReservationRepository reservationRepository,
        WaitlistRepository waitlistRepository,
        SlotRepository slotRepository,
        ReservationTimeRepository timeRepository,
        ThemeRepository themeRepository,
        Clock clock,
        ReservationWriter reservationWriter,
        WaitlistWriter waitlistWriter,
        WaitlistOrderPolicy waitlistOrderPolicy
    ) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.slotRepository = slotRepository;
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

    public List<ReservationWithStatus> getReservationsWithStatus() {
        List<ReservationWithStatus> results = new ArrayList<>();

        for (Reservation reservation : reservationRepository.findAll()) {
            results.add(ReservationWithStatus.reserved(reservation));
        }

        List<Waitlist> waitlists = waitlistRepository.findAll();
        addWaitingReservations(results, waitlists, waitlists);

        results.sort(Comparator.comparing(ReservationWithStatus::getDate).reversed()
            .thenComparing(reservation -> reservation.getTime().getStartAt()));

        return results;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<ReservationWithStatus> getMyReservations(String name) {
        List<ReservationWithStatus> results = new ArrayList<>();

        for (Reservation reservation : reservationRepository.findByName(name)) {
            results.add(ReservationWithStatus.reserved(reservation));
        }

        List<Waitlist> waitlists = waitlistRepository.findByName(name);
        List<Long> slotIds = waitlists.stream()
            .map(waitlist -> waitlist.getSlot().getId())
            .distinct()
            .toList();
        List<Waitlist> sameSlotWaitlists = waitlistRepository.findBySlotIds(slotIds);
        addWaitingReservations(results, waitlists, sameSlotWaitlists);

        results.sort(Comparator.comparing(ReservationWithStatus::getDate).reversed()
            .thenComparing(reservation -> reservation.getTime().getStartAt()));

        return results;
    }

    private void addWaitingReservations(
        List<ReservationWithStatus> results,
        List<Waitlist> targetWaitlists,
        List<Waitlist> orderSourceWaitlists
    ) {
        Map<Long, List<Waitlist>> waitlistsBySlotId = orderSourceWaitlists.stream()
            .collect(Collectors.groupingBy(waitlist -> waitlist.getSlot().getId()));

        for (Waitlist waitlist : targetWaitlists) {
            List<Waitlist> sameSlotWaitlists = waitlistsBySlotId.getOrDefault(waitlist.getSlot().getId(), List.of());
            int waitingOrder = waitlistOrderPolicy.calculateOrder(waitlist, sameSlotWaitlists);
            results.add(ReservationWithStatus.waiting(waitlist, waitingOrder));
        }
    }

    public Reservation getReservation(Long id) {
        return reservationRepository.getById(id, "존재하지 않는 예약입니다.");
    }

    public ReservationWithStatus reserveOrWait(ReservationRequest request) {
        ReservationTime reservationTime = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        Slot requestedSlot = Slot.of(request.date(), reservationTime, theme);
        LocalDateTime now = LocalDateTime.now(clock);

        if (requestedSlot.isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점에 예약할 수 없습니다.");
        }

        try {
            Reservation saved = reservationWriter.save(request.name(), requestedSlot);
            return ReservationWithStatus.reserved(saved);
        } catch (DataIntegrityViolationException e) {
            return waitlistWriter.save(request.name(), requestedSlot);
        }
    }

    private ReservationTime getReservationTime(Long timeId) {
        return timeRepository.findById(timeId)
            .orElseThrow(() -> new RoomEscapeException(
                RESERVATION_TIME_NOT_FOUND,
                "예약할 수 없는 시간입니다."
            ));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomEscapeException(
                THEME_NOT_FOUND,
                "예약할 수 없는 테마입니다."
            ));
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservation(id);
        cancelAndPromoteNextWaitlist(reservation);
    }

    @Transactional
    public void cancelMyReservation(Long id, String name) {
        Reservation reservation = getReservation(id);
        reservation.verifyCancelableBy(name, LocalDateTime.now(clock));
        cancelAndPromoteNextWaitlist(reservation);
    }

    private void cancelAndPromoteNextWaitlist(Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
        reservationRepository.flush();

        List<Waitlist> sameSlotWaitlists = waitlistRepository.findBySlotId(reservation.getSlot().getId());
        Optional<Waitlist> firstWaitlist = waitlistOrderPolicy.selectPromotionTarget(sameSlotWaitlists);

        if (firstWaitlist.isEmpty()) {
            return;
        }

        Waitlist promotedReservation = firstWaitlist.get();

        Reservation updated = new Reservation(
            promotedReservation.getMember(),
            promotedReservation.getSlot()
        );

        reservationRepository.save(updated);
        waitlistRepository.deleteById(promotedReservation.getId());
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
        Slot slot = slotRepository.getOrCreate(updated.getSlot());

        return reservationRepository.save(new Reservation(updated.getId(), updated.getMember(), slot));
    }

    private void verifyNoConflict(Reservation reservation) {
        if (reservationRepository.existsBy(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 시점·테마에 예약이 존재합니다.");
        }
    }
}
