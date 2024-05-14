package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignUpRequest;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public Long save(MemberSignUpRequest memberSignUpRequest) {
        Member member = memberSignUpRequest.toMember();
        if (memberRepository.existEmail(member)) {
            throw new IllegalArgumentException("중복된 이메일 입니다.");
        }

        return memberRepository.save(member);
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::toResponse)
                .toList();
    }

    public String createMemberToken(MemberLoginRequest memberLoginRequest) {
        Member member = findByEmailAndPassword(memberLoginRequest);

        return tokenProvider.createToken(member);
    }

    public Member findByEmailAndPassword(MemberLoginRequest memberLoginRequest) {
        return memberRepository.findByEmailAndPassword(memberLoginRequest.email(), memberLoginRequest.password())
                .orElseThrow(() -> new IllegalArgumentException("일치하지 않는 이메일 또는 비밀번호입니다."));
    }

    public MemberResponse findMemberNameByLoginMember(LoginMemberInToken loginMemberInToken) {
        Member member = memberRepository.findById(loginMemberInToken.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return MemberResponse.toResponse(member);
    }
}
