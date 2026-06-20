package roomescape.service;

import static roomescape.domain.exception.DomainErrorCode.DUPLICATE_RESERVATION;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWithStatus;
import roomescape.domain.Slot;
import roomescape.domain.Waitlist;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.SlotRepository;
import roomescape.repository.WaitlistRepository;

@Service
@Transactional(readOnly = true)
public class WaitlistWriter {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final SlotRepository slotRepository;
    private final WaitlistOrderPolicy waitlistOrderPolicy;
    private final Clock clock;
    private final MemberResolver memberResolver;

    public WaitlistWriter(
        ReservationRepository reservationRepository,
        WaitlistRepository waitlistRepository,
        SlotRepository slotRepository,
        WaitlistOrderPolicy waitlistOrderPolicy,
        Clock clock,
        MemberResolver memberResolver) {
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
        this.slotRepository = slotRepository;
        this.waitlistOrderPolicy = waitlistOrderPolicy;
        this.clock = clock;
        this.memberResolver = memberResolver;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationWithStatus save(String name, Slot slot) {
        Member member = memberResolver.resolve(name);
        Slot foundSlot = slotRepository.getOrCreate(slot);

        slotRepository.lockById(foundSlot.getId());

        Reservation reservationWithSlot = new Reservation(member, foundSlot);
        verifyNoDuplicateReservation(reservationWithSlot);

        LocalDateTime createdAt = LocalDateTime.now(clock);
        Long savedId = waitlistRepository.save(reservationWithSlot, createdAt);

        Waitlist waitlist = waitlistRepository.getById(savedId, "존재하지 않는 예약 대기입니다.");
        int waitingOrder = calculateWaitingOrder(waitlist);

        return ReservationWithStatus.waiting(waitlist, waitingOrder);
    }

    private int calculateWaitingOrder(Waitlist waitlist) {
        List<Waitlist> sameSlotWaitlists = waitlistRepository.findBySlotId(waitlist.getSlot().getId());

        return waitlistOrderPolicy.calculateOrder(waitlist, sameSlotWaitlists);
    }

    private void verifyNoDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "이미 같은 예약이 존재합니다.");
        }
        if (waitlistRepository.existsBySameUser(reservation)) {
            throw new RoomEscapeException(DUPLICATE_RESERVATION, "같은 슬롯에 중복 대기가 존재합니다.");
        }
    }
}
