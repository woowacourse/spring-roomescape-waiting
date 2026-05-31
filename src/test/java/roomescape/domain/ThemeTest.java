package roomescape.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;

class ThemeTest {

    private static final String VALID_DESCRIPTION = "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!";
    private static final String VALID_THUMBNAIL = "https://picsum.photos/seed/roomescape1/800/600.jpg";

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
}
