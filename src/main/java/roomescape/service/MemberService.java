package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.util.TokenProvider;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Member findMemberByToken(String token) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
        return findMemberById(memberId);
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> new NotFoundException("[ERROR] 멤버가 존재하지 않습니다."));
    }
}
