package roomescape.auth.service;

import org.springframework.stereotype.Component;
import roomescape.member.domain.Member;

@Component
public class AuthServiceValidator {

    public void checkInvalidAuthInfo(Member member, String password) {
        if (member.hasNotSamePassword(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호를 잘못 입력했습니다. 다시 입력해주세요.");
        }
    }
}
