package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.controller.dto.LoginRequest;
import roomescape.auth.controller.dto.SignUpRequest;
import roomescape.auth.controller.dto.TokenResponse;
import roomescape.auth.domain.AuthInfo;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ConflictException;
import roomescape.exception.custom.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public void authenticate(LoginRequest loginRequest) {
        Member findMember = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadRequestException("해당 유저를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(loginRequest.password(), findMember.getPassword())) {
            throw new BadRequestException("해당 유저를 찾을 수 없습니다.");
        }
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        return new TokenResponse(tokenProvider.createAccessToken(loginRequest.email()));
    }

    public AuthInfo fetchByToken(String token) {
        Member member = memberRepository.findByEmail(tokenProvider.getPayload(token).getValue())
                .orElseThrow(() -> new UnauthorizedException("토큰이 유효하지 않습니다."));
        return AuthInfo.of(member);
    }

    public void signUp(SignUpRequest signUpRequest) {
        if (memberRepository.existsByEmail(signUpRequest.email())) {
            throw new ConflictException("중복된 이메일입니다.");
        }
        memberRepository.save(
                new Member(signUpRequest.name(), signUpRequest.email(), passwordEncoder.encode(signUpRequest.password()), Role.USER));
    }
}
