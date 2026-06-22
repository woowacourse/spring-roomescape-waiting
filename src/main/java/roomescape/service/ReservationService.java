package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.SlotRepository;
import roomescape.domain.reservation.Status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final Clock clock;
    private final ReservationAssembler assembler;
    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            Clock clock,
            ReservationAssembler assembler,
            ReservationRepository reservationRepository,
            SlotRepository slotRepository,
            MemberRepository memberRepository
    ) {
        this.clock = clock;
        this.assembler = assembler;
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateCommand command) {
        Reservation assembled = assembler.from(command);
        Slot slot = slotRepository.getByIdForUpdate(assembled.getSlot().getId());

        Reservations existing = new Reservations(reservationRepository.findBySlot_Id(slot.getId()));
        Reservation joined = existing.join(assembled);
        return reservationRepository.save(joined);
    }

    public ReservationWithRank find(long id) {
        return reservationRepository.getByIdWithRank(id);
    }

    public Reservations findAll(Long memberId) {
        if (memberId == null) {
            return new Reservations(reservationRepository.findAll());
        }
        Member member = memberRepository.getById(memberId);
        return new Reservations(reservationRepository.findAllByMember(member));
    }

    public List<ReservationWithRank> findMine(Long memberId) {
        memberRepository.getById(memberId); // 존재 여부 확인
        return reservationRepository.findAllByMemberIdWithRank(memberId);
    }

    @Transactional
    public ReservationWithRank update(ReservationUpdateCommand command, long id) {
        Reservation existing = reservationRepository.getById(id);
        Reservation assembled = assembler.from(command);

        Long newSlotId = assembled.getSlot().getId();
        Long oldSlotId = existing.getSlotId();
        slotRepository.getByIdForUpdate(newSlotId);
        if (!oldSlotId.equals(newSlotId)) {
            slotRepository.getByIdForUpdate(oldSlotId);
        }

        Slot newSlot = assembled.getSlot();

        Reservations slotReservations = new Reservations(reservationRepository.findBySlot_Id(newSlot.getId())).excluding(id);
        Reservation template = slotReservations.join(assembled);

        boolean wasApproved = existing.isApproved();
        existing.changeSlot(template.getSlot());
        existing.changeStatus(template.getStatus());

        boolean slotChanged = !oldSlotId.equals(newSlot.getId());
        if (slotChanged && wasApproved) {
            promoteFirstWaiting(oldSlotId);
        }

        long rank = reservationRepository.findRankById(id);
        return new ReservationWithRank(existing, rank);
    }

    @Transactional
    public void cancel(long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.getById(reservationId);
        slotRepository.getByIdForUpdate(reservation.getSlotId());
        LocalDateTime now = LocalDateTime.now(clock);

        reservation.validateCancellable(now);
        reservation.validateOwner(memberId);

        reservationRepository.deleteById(reservationId);

        if (reservation.isApproved()) {
            promoteFirstWaiting(reservation.getSlotId());
        }
    }

    private void promoteFirstWaiting(Long slotId) {
        new Reservations(reservationRepository.findBySlot_Id(slotId))
                .firstWaiting()
                .ifPresent(waiting -> waiting.changeStatus(Status.APPROVED));
    }
}
