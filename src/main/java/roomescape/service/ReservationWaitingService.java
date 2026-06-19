package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.MemberErrorCode;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ReservationWaitingErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.response.ReservationWaitingResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository,
                                     ReservationRepository reservationRepository,
                                     MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationWaitingResponse createReservationWaiting(CreateReservationWaitingCommand command, LocalDateTime now) {
        Member member = getMember(command.memberId());
        ReservationTime reservationTime = getTime(command.timeId());
        Theme theme = getTheme(command.themeId());
        ReservationSlot slot = new ReservationSlot(command.reservationDate(), reservationTime, theme);

        validateUniqueReservationWaiting(member, slot);
        validateAlreadyReservationInSlot(member, slot);
        validatePastDatetime(slot, now);
        lockReservationForSlot(slot);

        ReservationWaiting reservationWaiting = ReservationWaiting.createWithoutId(member, now, slot);
        ReservationWaiting savedReservationWaiting = reservationWaitingRepository.save(reservationWaiting);

        int order = reservationWaitingRepository.countOrder(slot, savedReservationWaiting.getId());
        return ReservationWaitingResponse.from(savedReservationWaiting, order);
    }

    @Transactional
    public void promoteFirstWaiting(ReservationSlot slot) {
        reservationWaitingRepository.findFirstBySlotOrderByCreatedAt(slot).ifPresent(waiting -> {
            reservationRepository.save(Reservation.createWithoutId(waiting.getMember(), slot));
            reservationWaitingRepository.deleteById(waiting.getId());
        });
    }

    @Transactional
    public void delete(Long reservationWaitingId) {
        if (!reservationWaitingRepository.existsById(reservationWaitingId)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_WAITING_NOT_FOUND);
        }
        reservationWaitingRepository.deleteById(reservationWaitingId);
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeException(MemberErrorCode.NOT_FOUND));
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.NOT_FOUND));
    }

    private void lockReservationForSlot(ReservationSlot slot) {
        reservationRepository.findBySlotForUpdate(slot)
                .orElseThrow(() -> new RoomEscapeException(ReservationWaitingErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateUniqueReservationWaiting(Member member, ReservationSlot slot) {
        if (reservationWaitingRepository.existsByMemberAndSlot(member, slot)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.DUPLICATE);
        }
    }

    private void validateAlreadyReservationInSlot(Member member, ReservationSlot slot) {
        if (reservationRepository.existsByMemberAndSlot(member, slot)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.ALREADY_RESERVED);
        }
    }

    private void validatePastDatetime(ReservationSlot slot, LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationWaitingErrorCode.PAST_DATETIME);
        }
    }
}
