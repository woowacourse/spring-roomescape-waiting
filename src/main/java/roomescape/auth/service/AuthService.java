package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.domain.Token;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.provider.model.TokenProvider;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public AuthService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    public Token login(LoginRequest loginRequest) {
        Email email = Email.emailFrom(loginRequest.email());
        Password password = Password.passwordFrom(loginRequest.password());

        Member member = memberRepository.findMemberByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_NOT_EXIST_EXCEPTION));

        return tokenProvider.getAccessToken(member.getId());
    }
}
