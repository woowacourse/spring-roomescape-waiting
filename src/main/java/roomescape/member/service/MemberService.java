package roomescape.member.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.RegistrationRequest;
import roomescape.member.dto.TokenResponse;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse createToken(LoginRequest loginRequest) {
        Optional<Member> memberOptional = memberRepository.findByEmailAndPassword(loginRequest.email(),
                loginRequest.password());
        if (memberOptional.isEmpty()) {
            throw new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS);
        }
        String token = jwtTokenProvider.createToken(memberOptional.get());
        return new TokenResponse(token);
    }

    public MemberResponse findMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS));
        return new MemberResponse(member.getId(), member.getName());
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.UNAUTHORIZED_LOGIN_ACCESS));
    }

    public void signup(RegistrationRequest registrationRequest) {
        if (memberRepository.existsByEmail(registrationRequest.email())) {
            throw new ConflictException(ExceptionCause.MEMBER_DUPLICATE_EMAIL);
        }
        memberRepository.save(registrationRequest.createMember());
    }
}
