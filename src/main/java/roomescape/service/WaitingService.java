package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.login.LoginMember;
import roomescape.dto.waiting.WaitingRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            MemberRepository memberRepository,
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<WaitingResponse> getAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public Long addWaiting(WaitingRequest waitingRequest) {
        Member member = findMember(waitingRequest.memberId());
        Reservation reservation = findReservation(waitingRequest);
        Waiting waiting = new Waiting(member, reservation);
        waitingRepository.save(waiting);

        return waiting.getId();
    }

    @Transactional
    public void deleteWaiting(Long id, LoginMember loginMember) {
        Waiting waiting = findWaiting(id);
        Member member = findMember(loginMember.id());
        validateWaitingOwner(member, waiting);

        waitingRepository.delete(waiting);
    }

    public void deleteWaitingByAdmin(Long id) {
        Waiting waiting = findWaiting(id);

        waitingRepository.delete(waiting);
    }

    private Reservation findReservation(WaitingRequest waitingRequest) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                waitingRequest.date(),
                waitingRequest.timeId(),
                waitingRequest.themeId()
        ).orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 예약입니다."));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 사용자 정보 입니다.",
                        new Throwable("member_id : " + memberId)
                ));
    }

    private Waiting findWaiting(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 대기 정보 입니다.",
                        new Throwable("waiting_id : " + waitingId)
                ));
    }

    private void validateWaitingOwner(Member member, Waiting waiting) {
        if (waiting.isNotOwner(member)) {
            throw new IllegalArgumentException(
                    "[ERROR] 자신의 예약 대기만 삭제할 수 있습니다.",
                    new Throwable("waiting_id : " + waiting.getId())
            );
        }
    }
}
