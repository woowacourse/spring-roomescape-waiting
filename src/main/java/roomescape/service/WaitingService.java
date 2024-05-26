package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
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

    public Long addWaiting(WaitingRequest waitingRequest) {
        Member member = memberRepository.getById(waitingRequest.memberId());
        Reservation reservation = reservationRepository.getByDateAndTimeIdAndThemeId(
                waitingRequest.date(),
                waitingRequest.timeId(),
                waitingRequest.themeId()
        );

        Waiting waiting = new Waiting(member, reservation);
        waitingRepository.save(waiting);
        return waiting.getId();
    }

    public void deleteWaitingByAdmin(Long id) {
        Waiting waiting = waitingRepository.getById(id);

        waitingRepository.delete(waiting);
    }

    public void deleteWaiting(Long id, LoginMember loginMember) {
        Waiting waiting = waitingRepository.getById(id);
        Member member = memberRepository.getById(loginMember.id());
        validateWaitingOwner(member, waiting);

        waitingRepository.delete(waiting);
    }

    private void validateWaitingOwner(Member member, Waiting waiting) {
        if (waiting.isNotOwner(member)) {
            throw new IllegalArgumentException(
                    "[ERROR] 자신의 예약 대기만 삭제할 수 있습니다.",
                    new Throwable("waiting_id : " + waiting.getId())
            );
        }
    }

    public List<WaitingResponse> getAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
