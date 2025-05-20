package roomescape.login.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.login.application.dto.LoginCheckResponse;
import roomescape.login.application.dto.LoginRequest;
import roomescape.login.application.dto.SignupRequest;
import roomescape.login.application.dto.Token;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.domain.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class LoginService {

    private final MemberRepository memberRepository;
    private final JwtHandler jwtHandler;

    public LoginService(final MemberRepository memberRepository, final JwtHandler jwtHandler) {
        this.memberRepository = memberRepository;
        this.jwtHandler = jwtHandler;
    }

    public Token login(final LoginRequest loginRequest) {
        final Member member = memberRepository.findByEmailAndPassword(new Email(loginRequest.email()),
                        new Password(loginRequest.password()))
                .orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));

        return jwtHandler.createToken(member);
    }

    public LoginCheckResponse checkLogin(final LoginCheckRequest request) {
        final Member member = memberRepository.findById(request.id())
                .orElseThrow(() -> new NotFoundException("회원 정보가 존재하지 않습니다."));

        return LoginCheckResponse.from(member);
    }

    @Transactional
    public LoginCheckResponse signup(final SignupRequest request) {
        final Member member = request.toMember();
        validateDuplicatedEmail(member);
        final Member savedMember = memberRepository.save(member);
        return LoginCheckResponse.from(savedMember);
    }

    private void validateDuplicatedEmail(final Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new BadRequestException("이미 존재하는 이메일입니다.");
        }
    }
}

