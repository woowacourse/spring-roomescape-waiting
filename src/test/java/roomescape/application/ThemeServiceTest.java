package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.BasicAcceptanceTest;
import roomescape.exception.RoomescapeException;

class ThemeServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ThemeService themeService;

    @DisplayName("존재하지 않는 id로 테마를 삭제하면 예외가 발생한다.")
    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeleteWithNonExistId() {
        assertThatCode(() -> themeService.deleteById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
