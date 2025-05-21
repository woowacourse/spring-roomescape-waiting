package roomescape.application.reservation.command;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.WaitingException;

@Service
@Transactional
public class WaitingPromotionService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public WaitingPromotionService(WaitingRepository waitingRepository,
                                   ReservationRepository reservationRepository,
                                   MemberRepository memberRepository,
                                   Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Long approve(Long waitingId, Long memberId) {
        Member member = getMember(memberId);
        Waiting waiting = getWaiting(waitingId);
        validateControlPermission(member, waiting);
        validateReservationIsAvailable(waiting);
        validateWaitingIsFirst(waiting);
        Reservation savedReservation = promoteWaitingToReservation(waiting);
        return savedReservation.getId();
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new WaitingException("회원 정보가 존재하지 않습니다."));
    }

    private Waiting getWaiting(Long waitingId) {
        return waitingRepository.findByIdWithThemeAndTime(waitingId)
                .orElseThrow(() -> new WaitingException("대기 정보가 존재하지 않습니다."));
    }

    private void validateControlPermission(Member member, Waiting waiting) {
        if (!waiting.hasControlPermission(member)) {
            throw new WaitingException("대기 승인 권한이 없습니다.");
        }
    }

    private void validateReservationIsAvailable(Waiting waiting) {
        boolean alreadyReserved = reservationRepository.existsByDateAndTimeIdAndThemeId(
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId()
        );
        if (alreadyReserved) {
            throw new WaitingException("예약이 이미 존재하여 승인할 수 없습니다.");
        }
    }

    private void validateWaitingIsFirst(Waiting waiting) {
        int rank = waitingRepository.countWaitingsBeforeCreatedAt(
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId(),
                waiting.getCreatedAt()
        );
        if (rank > 0) {
            throw new WaitingException("승인 가능한 첫 번째 대기가 아닙니다.");
        }
    }

    private Reservation promoteWaitingToReservation(Waiting waiting) {
        waitingRepository.delete(waiting);
        Reservation reservation = createReservationBy(waiting);
        reservation.validateReservable(LocalDateTime.now(clock));
        return reservationRepository.save(reservation);
    }

    private Reservation createReservationBy(Waiting waiting) {
        return new Reservation(
                waiting.getMember(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme()
        );
    }
}
