package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dto.request.LoginRequest;
import roomescape.entity.Member;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.exception.custom.NotFoundException;
import roomescape.provider.JwtTokenProvider;
import roomescape.repository.jpa.JpaMemberRepository;

@Service
public class AuthService {

    private final JpaMemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JpaMemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("member"));

        validatePassword(request.password(), member);
        return jwtTokenProvider.createToken(member);
    }

    private void validatePassword(String password, Member member) {
        if (!password.equals(member.getPassword())) {
            throw new AuthenticatedException();
        }
    }

    public Member findMemberByToken(String token) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("member"));
    }
}
