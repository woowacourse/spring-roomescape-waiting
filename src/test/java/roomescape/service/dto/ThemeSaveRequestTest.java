package roomescape.service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.service.dto.request.ThemeSaveRequest;

import static org.assertj.core.api.Assertions.assertThatCode;

public class ThemeSaveRequestTest {

    @Test
    @DisplayName("테마 이름이 정상 입력될 경우 성공한다.")
    void checkThemeNameBlank_Success() {
        assertThatCode(() -> new ThemeSaveRequest("capy", "description", "thumbnail"))
                .doesNotThrowAnyException();
    }
}
