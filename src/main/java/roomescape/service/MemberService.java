package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.MemberErrorCode;
import roomescape.domain.Member;
import roomescape.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member save(String name) {
        return memberRepository.save(Member.createWithoutId(name));
    }

    public Member findByName(String name) {
        return memberRepository.findByName(name)
                .orElseThrow(() -> new RoomEscapeException(MemberErrorCode.NOT_FOUND));
    }
}
