package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.member.RegistrationRequest;
import roomescape.exception.DuplicateContentException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("사용자 생성")
class SignupServiceTest {

    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final SignupService signupService = new SignupService(memberRepository);

    @DisplayName("사용자를 생성할 수 있다")
    @Test
    void signupTest() {
        // given
        RegistrationRequest request = new RegistrationRequest("브라운", "brown@gmail.com", "wooteco7");

        // when & then
        assertThatNoException().isThrownBy(() -> signupService.signup(request));
    }

    @DisplayName("동일한 이메일의 사용자를 중복 생성할 수 없다")
    @Test
    void signupDuplicateTest() {
        // given
        RegistrationRequest request = new RegistrationRequest("브라운", "brown@gmail.com", "wooteco7");

        // when
        signupService.signup(request);

        // then
        assertThatThrownBy(() -> signupService.signup(request)).isInstanceOf(DuplicateContentException.class);
    }
}
