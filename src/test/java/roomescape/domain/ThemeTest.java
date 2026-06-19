package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class ThemeTest {

    @DisplayName("테마 정보를 저장한다.")
    @Test
    void create() {
        Theme theme = new Theme(1L, "잠긴 방", "닫힌 문을 여는 테마", "https://example.com/theme.jpg", 20000);

        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("잠긴 방");
        assertThat(theme.getDescription()).isEqualTo("닫힌 문을 여는 테마");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://example.com/theme.jpg");
    }

    @DisplayName("ID가 null이어도 아직 저장 전 도메인으로 생성할 수 있다.")
    @Test
    void nullId() {
        Theme theme = new Theme(null, "잠긴 방", "설명", "https://example.com/theme.jpg", 20000);

        assertThat(theme.getId()).isNull();
    }

    @DisplayName("테마 이름, 설명, 썸네일 URL은 null, 빈 문자열, 공백일 수 없다.")
    @Test
    void requiredFields() {
        assertInvalidInput(() -> new Theme(1L, null, "설명", "https://example.com/theme.jpg", 20000));
        assertInvalidInput(() -> new Theme(1L, "", "설명", "https://example.com/theme.jpg", 20000));
        assertInvalidInput(() -> new Theme(1L, "   ", "설명", "https://example.com/theme.jpg", 20000));

        assertInvalidInput(() -> new Theme(1L, "잠긴 방", null, "https://example.com/theme.jpg", 20000));
        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "", "https://example.com/theme.jpg", 20000));
        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "   ", "https://example.com/theme.jpg", 20000));

        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "설명", null, 20000));
        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "설명", "", 20000));
        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "설명", "   ", 20000));
        assertInvalidInput(() -> new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg", 0));
    }

    private void assertInvalidInput(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.INVALID_INPUT);
    }
}
