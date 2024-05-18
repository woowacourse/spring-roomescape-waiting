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
        Email email = Email.from(loginRequest.email());
        Password password = Password.from(loginRequest.password());

        Member member = memberRepository.findMemberByEmailAndPassword(email, password)
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.ID_AND_PASSWORD_NOT_MATCH_OR_EXIST));

        return tokenProvider.getAccessToken(member.getId());
    }
}
