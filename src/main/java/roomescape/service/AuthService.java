package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.AuthorizationException;
import roomescape.model.member.Member;
import roomescape.model.member.MemberEmail;
import roomescape.model.member.MemberPassword;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthDto;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.util.TokenManager;

@Service
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String tryLogin(AuthDto authDto) {
        MemberEmail email = authDto.getEmail();
        MemberPassword password = authDto.getPassword();
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new AuthorizationException("[ERROR] 해당 이메일과 비밀번호에 일치하는 계정이 없습니다."));
        MemberWithoutPassword loginMember = MemberWithoutPassword.from(member);
        return TokenManager.create(loginMember);
    }

    public MemberWithoutPassword extractLoginMember(String token) {
        return TokenManager.parse(token);
    }
}
