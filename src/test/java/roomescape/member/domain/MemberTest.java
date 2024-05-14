package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

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
}
