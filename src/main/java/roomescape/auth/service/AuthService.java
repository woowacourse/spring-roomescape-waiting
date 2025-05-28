package roomescape.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.exception.custom.NotFoundException;
import roomescape.auth.provider.JwtTokenProvider;
import roomescape.member.entity.Member;
import roomescape.member.repository.JpaMemberRepository;

@Service
public class AuthService {

    private final JpaMemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JpaMemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public String createToken(final LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("member"));

        member.validPassword(request.password());

        return jwtTokenProvider.createToken(member);
    }

    @Transactional(readOnly = true)
    public Member findMemberByToken(final String token) {
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member"));
    }
}
