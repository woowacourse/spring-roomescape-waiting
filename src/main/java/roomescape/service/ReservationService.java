package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Reservations;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;

import java.time.Clock;
import java.time.LocalDateTime;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final Clock clock;
    private final ReservationAssembler assembler;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            Clock clock,
            ReservationAssembler assembler,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository
    ) {
        this.clock = clock;
        this.assembler = assembler;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Reservation reserve(ReservationCreateCommand command) {
        Reservation assembled = assembler.from(command);
        Slot slot = assembled.getSlot();

        Reservations existing = new Reservations(reservationRepository.findBySlot_Id(slot.getId()));
        Reservation join = existing.join(assembled);
        return reservationRepository.save(join);
    }

    public Reservation find(long id) {
        Reservation reservation = reservationRepository.getById(id);
        Reservations slotReservations = new Reservations(reservationRepository.findBySlot_Id(reservation.getSlotId()));
        return reservation.withRank(slotReservations.rankOf(reservation));
    }

    public Reservations findAll(Long memberId) {
        if (memberId == null) {
            return new Reservations(reservationRepository.findAll());
        }
        Member member = memberRepository.getById(memberId);
        return new Reservations(reservationRepository.findAllByMember(member));
    }

    public Reservations findMine(Long memberId) {
        if(!memberRepository.existsById(memberId)){
            throw new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 회원을 찾을 수 없습니다. : " + memberId);
        }

        return new Reservations(reservationRepository.findByMemberId(memberId));
    }

    @Transactional
    public Reservation update(ReservationUpdateCommand command, long id) {
        Reservation existing = reservationRepository.getById(id);
        Reservation assembled = assembler.from(command);

        Slot newSlot = assembled.getSlot();
        Long oldSlotId = existing.getSlotId();

        Reservations slotReservations = new Reservations(reservationRepository.findBySlot_Id(newSlot.getId())).excluding(id);
        Reservation template = slotReservations.join(assembled);

        boolean wasApproved = existing.isApproved();
        existing.changeSlot(template.getSlot());
        existing.changeStatus(template.getStatus());

        boolean slotChanged = !oldSlotId.equals(newSlot.getId());
        if (slotChanged && wasApproved) {
            promoteFirstWaiting(oldSlotId);
        }

        return find(id);
    }

    @Transactional
    public void cancel(long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.getById(reservationId);
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
