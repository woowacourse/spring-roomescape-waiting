package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.LOGIN_FAIL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.Sha256Encryptor;
import roomescape.dto.LoginRequest;
import roomescape.dto.MemberInfo;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;

@SpringBootTest
class MemberServiceTest {

    public static final Sha256Encryptor SHA_256_ENCRYPTOR = new Sha256Encryptor();

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void cleanUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("잘못된 이메일이나 비밀번호로 로그인 시도할 경우 예외 발생하는지 확인")
    void loginWithInvalidRequest() {
        memberRepository.save(new Member("member", "email@email.com", SHA_256_ENCRYPTOR.encrypt("password")));

        assertThatThrownBy(() -> memberService.login(new LoginRequest("invalid@email.com", "invalid")))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(LOGIN_FAIL.getMessage());
    }

    @Test
    @DisplayName("사용자 아이디로 사용자 정보를 잘 조회하는지 확인")
    void findByMemberId() {
        Member member = memberRepository.save(
                new Member("member", "email@email.com", SHA_256_ENCRYPTOR.encrypt("password")));

        MemberInfo memberInfo = memberService.findByMemberId(member.getId());

        assertThat(memberInfo.id()).isEqualTo(member.getId());
    }
}
