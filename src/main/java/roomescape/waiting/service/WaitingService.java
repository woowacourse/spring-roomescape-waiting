package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationWaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository, MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public List<WaitingResponse> findWaitings() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public List<MyReservationWaitingResponse> findMyWaitings(Long memberId) {
        return waitingRepository.findByMember_idWithRank(memberId).stream()
                .map(MyReservationWaitingResponse::from)
                .toList();
    }

    private boolean isDuplicateWaiting(Waiting waiting) {
        return waitingRepository.existsByReservation_idAndMember_id(
                waiting.getReservation().getId(),
                waiting.getMember().getId());
    }

    public WaitingResponse createWaiting(WaitingCreateRequest request, Long waitingMemberId) {
        Reservation reservation =
                findReservationByDateAndTimeAndTheme(request.date(), request.timeId(), request.themeId());
        Member waitingMember = findMemberByMemberId(waitingMemberId);

        Waiting waiting = request.createWaiting(reservation, waitingMember);
        Waiting createdWaiting = createWaiting(waiting);

        return WaitingResponse.from(createdWaiting);
    }

    private Reservation findReservationByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTime_idAndTheme_id(date, timeId, themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약에 대해 대기할 수 없습니다."));
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
    }

    private Waiting createWaiting(Waiting waiting) {
        validateDuplicateWaiting(waiting);
        return waitingRepository.save(waiting);
    }

    private void validateDuplicateWaiting(Waiting waiting) {
        if (isDuplicateWaiting(waiting)) {
            throw new IllegalArgumentException("중복으로 예약 대기를 할 수 없습니다.");
        }
    }

    public void deleteWait(Long id) {
        waitingRepository.deleteById(id);
    }
}
