package roomescape.service;

import static roomescape.exception.ExceptionType.LOGIN_FAIL;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Sha256Encryptor;
import roomescape.dto.LoginRequest;
import roomescape.dto.UserInfo;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.MemberFixture;
import roomescape.repository.CollectionMemberRepository;
import roomescape.repository.MemberRepository;

class MemberServiceTest {

    public static final Sha256Encryptor SHA_256_ENCRYPTOR = new Sha256Encryptor();

    @Test
    @DisplayName("잘못된 이메일이나 비밀번호로 로그인 시도할 경우 예외 발생하는지 확인")
    void loginWithInvalidRequest() {
        MemberRepository memberRepository = new CollectionMemberRepository();
        MemberService memberService = new MemberService(memberRepository, SHA_256_ENCRYPTOR);
        Assertions.assertThatThrownBy(() -> memberService.login(new LoginRequest("email@email.com", "123456")))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(LOGIN_FAIL.getMessage());
    }

    @Test
    @DisplayName("사용자 아이디로 사용자 정보를 잘 조회하는지 확인")
    void findByUserId() {
        MemberRepository memberRepository = new CollectionMemberRepository(List.of(DEFAULT_MEMBER));
        MemberService memberService = new MemberService(memberRepository, SHA_256_ENCRYPTOR);

        Long memberId = DEFAULT_MEMBER.getId();
        UserInfo userInfo = memberService.findByUserId(memberId);
        Assertions.assertThat(userInfo)
                .isEqualTo(MemberFixture.DEFAULT_MEMBER_INFO);
    }
}
