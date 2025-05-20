package roomescape.member.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.PasswordEncoder;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MemberSignupRequest;
import roomescape.member.dto.MemberTokenResponse;
import roomescape.member.exception.EmailAlreadyExistsException;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.exception.PasswordNotMatchedException;
import roomescape.member.login.authorization.JwtTokenProvider;

@Service
@AllArgsConstructor
public class MemberService {

    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;


    public MemberResponse add(final MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        Member member = new Member(
                new Name(request.name()),
                new Email(request.email()),
                new Password(request.password(), passwordEncoder)
        );

        return MemberResponse.from(memberRepository.save(member));
    }

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    //TODO: 지우기  (2025-05-21, 수, 3:15)
    public MemberResponse findByEmail(final String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
        return MemberResponse.from(member);
    }

    public MemberResponse findByToken(final String token) {
        String email = jwtTokenProvider.getPayloadEmail(token);
        return findByEmail(email);
    }
    //

    public MemberTokenResponse createToken(final MemberLoginRequest memberLoginRequest) {
        Member member = memberRepository.findByEmail(memberLoginRequest.email())
                .orElseThrow(MemberNotFoundException::new);
        if (!passwordEncoder.matches(memberLoginRequest.password(), member.getPassword().getValue())) {
            throw new PasswordNotMatchedException();
        }
        String role = assignRole(memberLoginRequest.email()).getRole();
        String accessToken = jwtTokenProvider.createToken(memberLoginRequest.email(), role);
        return new MemberTokenResponse(accessToken);
    }

    private Role assignRole(String email) {
        Member member = memberRepository.findByEmail(email).get();
        return member.getRole();
    }
}
