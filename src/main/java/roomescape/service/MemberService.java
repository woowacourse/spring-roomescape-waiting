package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.controller.dto.TokenResponse;
import roomescape.controller.member.dto.LoginMember;
import roomescape.controller.member.dto.MemberLoginRequest;
import roomescape.controller.member.dto.SignupRequest;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.exception.InvalidRequestException;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.repository.MemberRepository;
import roomescape.service.exception.DuplicateEmailException;
import roomescape.service.exception.InvalidTokenException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EncryptionService encryptionService;

    public MemberService(final MemberRepository memberRepository, final JwtTokenProvider jwtTokenProvider,
                         final EncryptionService encryptionService) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.encryptionService = encryptionService;
    }

    public Member save(final SignupRequest request) {
        final Optional<Member> findMember = memberRepository.findByEmail(request.email());
        if (findMember.isPresent()) {
            throw new DuplicateEmailException("해당 email로 사용자가 존재합니다.");
        }
        final String encryptedPassword = encryptionService.encryptPassword(request.password());
        final Member member = new Member(null, request.name(), request.email(), encryptedPassword, Role.USER);
        return memberRepository.save(member);
    }

    public boolean invalidPassword(final String email, final String rawPassword) {
        final Member findMember = memberRepository.fetchByEmail(email);
        final String encryptedPassword = encryptionService.encryptPassword(rawPassword);
        return !encryptedPassword.equals(findMember.getPassword());
    }

    public TokenResponse createToken(final MemberLoginRequest request) {
        if (invalidPassword(request.email(), request.password())) {
            throw new InvalidRequestException("Invalid email or password");
        }
        final Member member = memberRepository.fetchByEmail(request.email());

        final Map<String, Object> payload = Map.of(
                "sub", String.valueOf(member.getId()),
                "name", member.getName(),
                "role", member.getRole().name()
        );

        final String accessToken = jwtTokenProvider.generateToken(payload);
        return new TokenResponse(accessToken);
    }

    public LoginMember findMemberByToken(final String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }
        final Map<String, Object> payload = jwtTokenProvider.getPayload(token);

        return new LoginMember(
                Long.parseLong((String) payload.get("sub")),
                (String) payload.get("name"),
                Role.valueOf((String) payload.get("role")));
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }
}
