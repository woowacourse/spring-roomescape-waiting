package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.model.member.Member;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthDto;
import roomescape.service.dto.MemberInfo;
import roomescape.util.TokenManager;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String createToken(AuthDto authDto) {
        Member member = memberRepository.findByEmailAndPassword(authDto.getEmail(), authDto.getPassword())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당하는 계정이 없습니다."));
        return TokenManager.create(member);
    }

    public MemberInfo checkToken(String token) {
        return TokenManager.parse(token);
    }
}
