package roomescape.application.reservation.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.WaitingRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.WaitingException;

@Service
@Transactional
public class DeleteWaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public DeleteWaitingService(WaitingRepository waitingRepository,
                                MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
    }

    public void cancel(Long waitingId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException("회원 정보가 존재하지 않습니다."));
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new WaitingException("대기 정보가 존재하지 않습니다."));
        if (!waiting.isOwner(member)) {
            throw new WaitingException("대기 취소 권한이 없습니다.");
        }
        waitingRepository.delete(waiting);
    }
}
