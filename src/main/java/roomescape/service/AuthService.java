package roomescape.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.entity.Member;
import roomescape.dto.request.LoginRequest;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.provider.JwtTokenProvider;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String createToken(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email());

        validatePassword(request.password(), member);
        return jwtTokenProvider.createToken(member);
    }

    private void validatePassword(String password, Member member) {
        if(!password.equals(member.getPassword())) {
            throw new AuthenticatedException();
        }
    }

    public Member findMemberByToken(String token) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("member"));
    }
}
