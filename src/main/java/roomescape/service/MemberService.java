package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.auth.JwtTokenProvider;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.domain.PasswordEncoder;
import roomescape.domain.dto.*;
import roomescape.exception.AccessNotAllowException;
import roomescape.exception.SignupFailException;
import roomescape.repository.MemberDao;

import java.util.List;

@Service
public class MemberService {
    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberService(final MemberDao memberDao, final PasswordEncoder passwordEncoder, final JwtTokenProvider jwtTokenProvider) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public MemberResponses findEntireMembers() {
        final List<MemberResponse> memberResponses = memberDao.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
        return new MemberResponses(memberResponses);
    }

    public SignupResponse createUser(final SignupRequest signupRequest) {
        validateExist(signupRequest);
        Password password = passwordEncoder.encode(signupRequest.password());
        Long id = memberDao.create(signupRequest, password);
        String accessToken = jwtTokenProvider.createToken(signupRequest.email());
        return new SignupResponse(id, accessToken);
    }

    private void validateExist(final SignupRequest signupRequest) {
        if (memberDao.isExist(signupRequest)) {
            throw new SignupFailException("회원 정보가 이미 존재합니다.");
        }
    }

    public TokenResponse login(final LoginRequest loginRequest) {
        Password password = memberDao.findPasswordByEmail(loginRequest.email())
                .orElseThrow(() -> new AccessNotAllowException("회원 정보가 일치하지 않습니다."));
        Password requestPassword = passwordEncoder.encode(loginRequest.password(), password.getSalt());
        if (!password.check(requestPassword)) {
            throw new AccessNotAllowException("회원 정보가 일치하지 않습니다.");
        }
        final String accessToken = jwtTokenProvider.createToken(loginRequest.email());
        return new TokenResponse(accessToken);
    }

    public Member getMemberInfo(final String accessToken) {
        final String payload = jwtTokenProvider.getPayload(accessToken);
        final Member member = memberDao.findByEmail(payload)
                .orElseThrow(() -> new AccessNotAllowException("회원 정보가 존재하지 않습니다."));
        return member;
    }
}
