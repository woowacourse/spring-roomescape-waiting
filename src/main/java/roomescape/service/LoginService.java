package roomescape.service;

import java.util.NoSuchElementException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberPassword;
import roomescape.repository.MemberRepository;
import roomescape.service.request.LoginRequest;

@Service
@Transactional(readOnly = true)
public class LoginService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public LoginService(final PasswordEncoder passwordEncoder, final MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    public Member login(final LoginRequest request) {
        Member member = memberRepository.findByEmail(new MemberEmail(request.email()))
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 멤버입니다."));
        MemberPassword rawPassword = new MemberPassword(request.password());
        if (!member.isMatchPassword(rawPassword, passwordEncoder)) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }
}
