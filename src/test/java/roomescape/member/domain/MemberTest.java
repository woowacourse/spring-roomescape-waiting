package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.Fixtures;
import roomescape.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("사용자")
class MemberTest {

    @DisplayName("사용자는 필드에 빈 값이 있으면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void validateNullOrEmptyField(String blank) {
        // when & then
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThatThrownBy(() -> new Member(blank, "email@gmail.com", "password"))
                .isInstanceOf(IllegalArgumentException.class);
        softAssertions.assertThatThrownBy(() -> new Member("name", blank, "password"))
                .isInstanceOf(IllegalArgumentException.class);
        softAssertions.assertThatThrownBy(() -> new Member("name", "email@gmail.com", blank))
                .isInstanceOf(IllegalArgumentException.class);

        softAssertions.assertAll();
    }

    @DisplayName("사용자는 잘못된 비밀번호가 들어오는 경우 예외가 발생한다.")
    @Test
    void validatePassword() {
        // given
        Member member = Fixtures.memberFixture;
        String wrongPassword = "wrongPassword";

        // when & then
        assertThatThrownBy(() -> member.validatePassword(wrongPassword))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 사용자 인증 정보입니다.");
    }
}
