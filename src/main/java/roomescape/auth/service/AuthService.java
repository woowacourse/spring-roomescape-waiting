package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.dto.*;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 멤버입니다."));
    }

    public LoginResponse login(LoginRequest request) {
        Member member = findMemberByEmail(request.email());

        validatePassword(request, member);

        String accessToken = jwtTokenProvider.generate(member);
        return new LoginResponse(accessToken);
    }

    private void validatePassword(LoginRequest request, Member member) {
        if (!member.getPassword().equals(request.password())) {
            throw new BadRequestException("비밀번호가 잘못됐습니다.");
        }
    }

    public LoginCheckResponse checkLogin(LoginMember loginMember) {
        Member member = findMemberByEmail(loginMember.email());
        return LoginCheckResponse.from(member);
    }

    public LoginCheckResponse signup(SignupRequest request) {
        Member member = request.toMember();

        validateDuplicatedEmail(member);

        Member savedMember = memberRepository.save(member);
        return LoginCheckResponse.from(savedMember);
    }

    private void validateDuplicatedEmail(Member member) {
        memberRepository.findByEmail(member.getEmail())
                .ifPresent((existsMember) -> {
                    throw new BadRequestException("이미 존재하는 이메일입니다.");
                });
    }
}
