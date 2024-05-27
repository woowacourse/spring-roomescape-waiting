package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.LoginMember;
import roomescape.dto.TokenInfo;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.infrastructure.TokenGenerator;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;

    public MemberService(MemberRepository memberRepository, TokenGenerator tokenGenerator) {
        this.memberRepository = memberRepository;
        this.tokenGenerator = tokenGenerator;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        Member member = memberRepository.findByEmailAndPassword(tokenRequest.email(), tokenRequest.password())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));
        String accessToken = tokenGenerator.createToken(new TokenInfo(member));
        return TokenResponse.from(accessToken);
    }

    public MemberResponse loginCheck(LoginMember loginMember) {
        Member member = memberRepository.getMemberById(loginMember.id());
        return MemberResponse.from(member);
    }

    public LoginMember findLoginMemberByToken(String token) {
        TokenInfo tokenInfo = tokenGenerator.extract(token);
        Member member = getMemberByEmail(tokenInfo.payload());
        return LoginMember.from(member);
    }

    public boolean hasAdminRole(String token) {
        TokenInfo tokenInfo = tokenGenerator.extract(token);
        Member member = getMemberByEmail(tokenInfo.payload());
        return member.isAdmin();
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 입니다"));
    }
}
