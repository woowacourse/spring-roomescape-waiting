package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.jwt.TokenProvider;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS));
    }
}
