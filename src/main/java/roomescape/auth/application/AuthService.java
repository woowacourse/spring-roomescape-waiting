package roomescape.auth.application;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.application.dto.LoginCheckResponse;
import roomescape.auth.application.dto.LoginRequest;
import roomescape.auth.exception.PasswordNotMatchedException;
import roomescape.member.domain.Member;
import roomescape.member.domain.PasswordEncoder;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(MemberNotFoundException::new);

        checkPasswordMatched(request, member);

        return tokenProvider.createToken(member);
    }

    private void checkPasswordMatched(LoginRequest request, Member member) {
        if (!passwordEncoder.matches(request.password(), member.getPassword().getValue())) {
            throw new PasswordNotMatchedException();
        }
    }

    public LoginCheckResponse loginCheck(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        return LoginCheckResponse.from(member);
    }
}
