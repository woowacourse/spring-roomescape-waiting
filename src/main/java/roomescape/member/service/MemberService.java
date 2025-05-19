package roomescape.member.service;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AuthenticationException;
import roomescape.common.exception.AuthorizationException;
import roomescape.common.exception.InvalidEmailException;
import roomescape.common.exception.message.LoginExceptionMessage;
import roomescape.common.exception.message.MemberExceptionMessage;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.dto.MemberTokenResponse;
import roomescape.member.login.authorization.JwtTokenProvider;

@Service
public class MemberService {

    private static final String EMAIL_FORMAT_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public MemberResponse findByEmail(final String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException(MemberExceptionMessage.INVALID_MEMBER_EMAIL.getMessage()));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public MemberResponse findByToken(final String token) {
        String email = jwtTokenProvider.getPayloadEmail(token);
        return findByEmail(email);
    }

    public MemberTokenResponse createToken(final MemberLoginRequest memberLoginRequest) {
        Member member = validateLoginEmail(memberLoginRequest);
        validatePassword(memberLoginRequest.password(), member.getPassword());

        String role = assignRole(memberLoginRequest.email()).getRole();
        String accessToken = jwtTokenProvider.createToken(memberLoginRequest.email(), role);
        return new MemberTokenResponse(accessToken);
    }

    private Member validateLoginEmail(final MemberLoginRequest memberLoginRequest) {
        return memberRepository.findByEmail(memberLoginRequest.email())
                .orElseThrow(() -> new AuthenticationException(LoginExceptionMessage.AUTHENTICATION_FAIL.getMessage()));
    }

    public MemberResponse add(final MemberSignupRequest memberSignupRequest) {
        validateSignupEmail(memberSignupRequest);
        validateDuplicateMember(memberSignupRequest);

        String hashedPassword = passwordEncoder.encode(memberSignupRequest.password());
        Member member = new Member(
                memberSignupRequest.name(),
                memberSignupRequest.email(),
                hashedPassword
        );
        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    private void validateSignupEmail(final MemberSignupRequest memberSignupRequest) {
        if (!memberSignupRequest.email().matches(EMAIL_FORMAT_REGEX)) {
            throw new InvalidEmailException(MemberExceptionMessage.INVALID_MEMBER_EMAIL_FORMAT.getMessage());
        }
    }

    private void validateDuplicateMember(final MemberSignupRequest memberSignupRequest) {
        if (memberRepository.existsByEmail(memberSignupRequest.email())) {
            throw new AuthorizationException(MemberExceptionMessage.DUPLICATE_MEMBER.getMessage());
        }
    }

    private Role assignRole(final String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException(MemberExceptionMessage.INVALID_MEMBER_EMAIL.getMessage()));
        return member.getRole();
    }

    private void validatePassword(final String plainPassword, final String hashedPassword) {
        if (!passwordEncoder.matches(plainPassword, hashedPassword)) {
            throw new AuthenticationException(LoginExceptionMessage.AUTHENTICATION_FAIL.getMessage());
        }
    }
}
