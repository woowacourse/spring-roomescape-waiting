package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PasswordEncoder;
import roomescape.domain.dto.LoginRequest;
import roomescape.domain.dto.MemberResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.domain.dto.SignupRequest;
import roomescape.domain.dto.SignupResponse;
import roomescape.domain.dto.TokenResponse;
import roomescape.exception.AccessNotAllowException;
import roomescape.exception.SignupFailException;
import roomescape.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(final MemberRepository memberRepository, final PasswordEncoder passwordEncoder,
                         final JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public ResponsesWrapper<MemberResponse> findEntireMembers() {
        final List<MemberResponse> memberResponses = memberRepository.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
        return new ResponsesWrapper<>(memberResponses);
    }

    public SignupResponse createUser(final SignupRequest signupRequest) {
        validateExist(signupRequest);
        Password password = passwordEncoder.encode(signupRequest.password());
        final Member member = memberRepository.save(signupRequest.toEntity(password));
        String accessToken = jwtTokenProvider.createToken(signupRequest.email());
        return new SignupResponse(member.getId(), accessToken);
    }

    private void validateExist(final SignupRequest signupRequest) {
        if (memberRepository.existsByEmail(signupRequest.email())) {
            throw new SignupFailException("회원 정보가 이미 존재합니다.");
        }
    }

    public TokenResponse login(final LoginRequest loginRequest) {
        final Member member = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new AccessNotAllowException("회원 정보가 일치하지 않습니다."));
        final Password password = member.getPassword();
        Password requestPassword = passwordEncoder.encode(loginRequest.password(), password.getSalt());
        if (!password.check(requestPassword)) {
            throw new AccessNotAllowException("회원 정보가 일치하지 않습니다.");
        }
        final String accessToken = jwtTokenProvider.createToken(loginRequest.email());
        return new TokenResponse(accessToken);
    }

    public Member getMemberInfo(final String accessToken) {
        final String payload = jwtTokenProvider.getPayload(accessToken);
        final Member member = memberRepository.findByEmail(payload)
                .orElseThrow(() -> new AccessNotAllowException("회원 정보가 존재하지 않습니다."));
        return member;
    }
}
