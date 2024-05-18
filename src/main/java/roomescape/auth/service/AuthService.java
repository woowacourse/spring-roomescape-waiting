package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(TokenProvider tokenProvider, MemberRepository memberRepository) {
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
    }

    public String createToken(LoginRequest request) {
        Email email = new Email(request.email());
        Password password = new Password(request.password());
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new BadArgumentRequestException("해당 멤버가 존재하지 않습니다."));
        return tokenProvider.createToken(member.getId());
    }

    public LoggedInMember findLoggedInMember(String token) {
        Long memberId = tokenProvider.findMemberId(token);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 멤버가 존재하지 않습니다."));
        return LoggedInMember.from(member);
    }
}
