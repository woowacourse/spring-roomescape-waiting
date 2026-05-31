package roomescape.domain.reservation.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.domain.reservation.theme.Description.DESCRIPTION_NAME_MAX_LENGTH;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DescriptionTest {

    @Test
    @DisplayName("올바른 정보로 테마 설명을 생성하면 성공한다.")
    void 테마_설명_생성_테스트() {
        String description = "우테코";

        assertDoesNotThrow(() -> new Description(description));
    }

    @Test
    @DisplayName("테마 설명이 빈칸 이면 예외가 발생한다")
    void 테마_이름_빈칸_예외_테스트() {
        String description = "";

        assertThatThrownBy(() -> new Description(description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 설명은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("테마 설명 글자 수 제한을 초과하면 예외가 발생한다.")
    void 테마_이름_글자_수_초과_예외_테스트() {
        String description = "0".repeat(DESCRIPTION_NAME_MAX_LENGTH+1);

        assertThatThrownBy(() -> new Description(description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 설명은 %d자를 초과할 수 없습니다.".formatted(DESCRIPTION_NAME_MAX_LENGTH));
    }
}
