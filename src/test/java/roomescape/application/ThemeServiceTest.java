package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import roomescape.BasicAcceptanceTest;
import roomescape.TestFixtures;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;

class ThemeServiceTest extends BasicAcceptanceTest {
    @Autowired
    private ThemeService themeService;

    @DisplayName("해당 id의 테마를 삭제한다.")
    @Test
    void deleteById() {
        themeService.deleteById(6L);
        List<ThemeResponse> themeResponses = themeService.findAll();

        assertThat(themeResponses).isEqualTo(TestFixtures.THEME_RESPONSES_4);
    }

    @DisplayName("존재하지 않는 id로 테마를 삭제하면 예외가 발생한다.")
    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeleteWithNonExistId() {
        assertThatCode(() -> themeService.deleteById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("httpStatus")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
