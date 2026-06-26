package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;

class ThemeTest {

    private static final String VALID_NAME = "무인도 탈출";
    private static final String VALID_DESCRIPTION = "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!";
    private static final String VALID_THUMBNAIL = "https://picsum.photos/seed/roomescape1/800/600.jpg";

    @Test
    @DisplayName("id가 null이어도 테마를 생성할 수 있다")
    void id가_null이어도_테마를_생성할_수_있다() {
        assertDoesNotThrow(() -> new Theme(null, VALID_NAME, VALID_DESCRIPTION, VALID_THUMBNAIL));
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void 이름이_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, null, VALID_DESCRIPTION, VALID_THUMBNAIL)
        );
        assertEquals("테마 이름은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이름이 빈문자열이면 예외가 발생한다")
    void 이름이_빈문자열이면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, "", VALID_DESCRIPTION, VALID_THUMBNAIL)
        );
    }

    @Test
    @DisplayName("이름이 공백만으로 이루어져 있으면 예외가 발생한다")
    void 이름이_공백만으로_이루어져_있으면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, "   ", VALID_DESCRIPTION, VALID_THUMBNAIL)
        );
    }

    @Test
    @DisplayName("이름이 30자를 초과하면 예외가 발생한다")
    void 이름이_30자를_초과하면_예외가_발생한다() {
        String name = "탈".repeat(31);
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, name, VALID_DESCRIPTION, VALID_THUMBNAIL)
        );
        assertEquals("테마 이름은 30자를 초과할 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("설명이 null이면 예외가 발생한다")
    void 설명이_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, VALID_NAME, null, VALID_THUMBNAIL)
        );
        assertEquals("테마 설명은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("설명이 빈문자열이면 예외가 발생한다")
    void 설명이_빈문자열이면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, VALID_NAME, "", VALID_THUMBNAIL)
        );
    }

    @Test
    @DisplayName("썸네일이 null이면 예외가 발생한다")
    void 썸네일이_null이면_예외가_발생한다() {
        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, VALID_NAME, VALID_DESCRIPTION, null)
        );
        assertEquals("테마 썸네일은 비어 있을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("썸네일이 빈문자열이면 예외가 발생한다")
    void 썸네일이_빈문자열이면_예외가_발생한다() {
        assertThrows(
                DomainValidationException.class,
                () -> new Theme(1L, VALID_NAME, VALID_DESCRIPTION, "")
        );
    }
}
