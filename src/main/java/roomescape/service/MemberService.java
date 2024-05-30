package roomescape.service;

import java.util.List;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.dto.LoginMember;
import roomescape.dto.request.TokenRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.TokenResponse;
import roomescape.infrastructure.TokenGenerator;
import roomescape.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;

    public MemberService(MemberRepository memberRepository, TokenGenerator tokenGenerator) {
        this.memberRepository = memberRepository;
        this.tokenGenerator = tokenGenerator;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 회원 입니다"));
    }

    @Transactional(readOnly = true)
    public LoginMember findLoginMemberByToken(String token) {
        String email = tokenGenerator.getPayload(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new JwtException("[ERROR] 존재하지 않는 회원 입니다"));
        return LoginMember.from(member);
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        Member member = memberRepository.findByEmailAndPassword(tokenRequest.email(), tokenRequest.password())
                .orElseThrow(() -> new JwtException("아이디 또는 비밀번호가 일치하지 않습니다."));
        String accessToken = tokenGenerator.createToken(tokenRequest.email(), member.getRole().name());
        return TokenResponse.from(accessToken);
    }

    public MemberResponse checkLogin(final LoginMember loginMember) {
        Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new JwtException("[ERROR] 로그인이 필요합니다."));
        return MemberResponse.from(member);
    }
}
