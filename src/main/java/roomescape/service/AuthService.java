package roomescape.service;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.controller.utils.TokenUtils;
import roomescape.model.member.Member;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.AuthDto;

@Service
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public String createToken(AuthDto authDto) {
        Member member = memberRepository.findByEmailAndPassword(authDto.getEmail(), authDto.getPassword())
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 해당하는 계정이 없습니다."));
        return TokenUtils.createToken(member);
    }
}
