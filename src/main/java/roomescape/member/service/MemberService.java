package roomescape.member.service;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import roomescape.common.exception.AuthenticationException;
import roomescape.common.exception.AuthorizationException;
import roomescape.common.exception.InvalidEmailException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.IdExceptionMessage;
import roomescape.common.exception.message.LoginExceptionMessage;
import roomescape.common.exception.message.MemberExceptionMessage;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.dto.MemberTokenResponse;
import roomescape.member.login.authorization.JwtTokenProvider;

@Service
public class MemberService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDao memberDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberService(JwtTokenProvider jwtTokenProvider, MemberDao memberDao) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDao = memberDao;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<MemberResponse> findAll() {
        return memberDao.findAll().stream()
                .map(member -> new MemberResponse(
                        member.getId(),
                        member.getName(),
                        member.getEmail())
                )
                .toList();
    }

    public MemberResponse findById(Long id) {
        Member member = memberDao.findById(id)
                .orElseThrow(() -> new InvalidIdException(IdExceptionMessage.INVALID_MEMBER_ID.getMessage()));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public MemberResponse findByEmail(final String email) {
        Member member = memberDao.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException(MemberExceptionMessage.INVALID_MEMBER_EMAIL.getMessage()));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public MemberResponse findByToken(final String token) {
        String email = jwtTokenProvider.getPayloadEmail(token);
        return findByEmail(email);
    }

    public MemberTokenResponse createToken(final MemberLoginRequest memberLoginRequest) {
        Member member = memberDao.findByEmail(memberLoginRequest.email())
                .orElseThrow(() -> new AuthenticationException(LoginExceptionMessage.AUTHENTICATION_FAIL.getMessage()));
        validatePassword(memberLoginRequest.password(), member.getPassword());
        String role = assignRole(memberLoginRequest.email()).getRole();
        String accessToken = jwtTokenProvider.createToken(memberLoginRequest.email(), role);
        return new MemberTokenResponse(accessToken);
    }

    public MemberResponse add(final MemberSignupRequest memberSignupRequest) {
        if (memberDao.existsByEmail(memberSignupRequest.email())) {
            throw new AuthorizationException(MemberExceptionMessage.DUPLICATE_MEMBER.getMessage());
        }
        String hashedPassword = passwordEncoder.encode(memberSignupRequest.password());
        Member member = new Member(
                memberSignupRequest.name(),
                memberSignupRequest.email(),
                hashedPassword
        );
        Member savedMember = memberDao.add(member);

        return new MemberResponse(
                savedMember.getId(),
                savedMember.getName(),
                savedMember.getEmail()
        );
    }

    private Role assignRole(String email) {
        Member member = memberDao.findByEmail(email).get();
        return member.getRole();
    }

    private void validatePassword(String plainPassword, String hashedPassword) {
        if (!passwordEncoder.matches(plainPassword, hashedPassword)) {
            throw new AuthenticationException(LoginExceptionMessage.AUTHENTICATION_FAIL.getMessage());
        }
    }
}
