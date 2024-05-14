package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMember;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberLoginService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberLoginService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createMemberToken(MemberLoginRequest memberLoginRequest) {
        Member member = findByEmailAndPassword(memberLoginRequest);

        return jwtTokenProvider.generateToken(member);
    }

    public Member findByEmailAndPassword(MemberLoginRequest memberLoginRequest) {
        return memberRepository.findByEmailAndPassword(memberLoginRequest.email(), memberLoginRequest.password())
                .orElseThrow(() -> new IllegalArgumentException("일치하지 않는 이메일 또는 비밀번호입니다."));
    }

    public MemberResponse findMemberNameByLoginMember(LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return MemberResponse.toResponse(member);
    }
}
