package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidDomainException;

/**
 * Theme 도메인 단위 테스트.
 * 보호 대상: 테마 객체의 정합성(이름·설명·썸네일 비어 있음 금지, 이름 길이 제한).
 */
class ThemeTest {

    @Nested
    @DisplayName("이름 제약")
    class Name {

        @Test
        @DisplayName("이름이 비어 있으면 예외")
        void 이름_공백() {
            assertThatThrownBy(() -> Theme.create(" ", "설명", "url"))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("테마 이름은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("이름이 30자를 초과하면 예외")
        void 이름_길이_초과() {
            String tooLong = "가".repeat(31);
            assertThatThrownBy(() -> Theme.create(tooLong, "설명", "url"))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("테마 이름은 30자를 초과할 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("설명·썸네일 제약")
    class DescriptionAndThumbnail {

        @Test
        @DisplayName("설명이 비어 있으면 예외")
        void 설명_공백() {
            assertThatThrownBy(() -> Theme.create("테마", " ", "url"))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("테마 설명은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("썸네일이 비어 있으면 예외")
        void 썸네일_공백() {
            assertThatThrownBy(() -> Theme.create("테마", "설명", " "))
                    .isInstanceOf(InvalidDomainException.class)
                    .hasMessage("테마 썸네일은 비어 있을 수 없습니다.");
        }

        @Test
        @DisplayName("모든 값이 유효하면 생성된다")
        void 정상_생성() {
            assertThatCode(() -> Theme.create("테마", "설명", "https://example.com/a.jpg"))
                    .doesNotThrowAnyException();
        }
    }
}
