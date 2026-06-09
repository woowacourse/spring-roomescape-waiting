package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.support.ConcurrentExecutor;
import roomescape.support.ConcurrentResult;
import roomescape.support.ServiceIntegrationTest;
import roomescape.theme.exception.DuplicateThemeException;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.service.dto.ThemeCommand;

public class ThemeServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    ThemeService themeService;

    @DisplayName("동일한 테마를 동시에 생성하면 하나만 성공하고 나머지는 중복 예외가 발생한다")
    @Test
    void registerThemeTest_duplicate() throws InterruptedException {
        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                themeService.registerTheme(new ThemeCommand(
                        "테마", "설명", "url"
                ));

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(DuplicateThemeException.class::isInstance);
    }

    @DisplayName("테마 삭제 요청이 동시에 들어오면 하나만 성공하고 나머지는 예외가 발생한다")
    @Test
    void removeThemeByIdTest_duplicate() throws InterruptedException {
        //given
        themeService.registerTheme(
                new ThemeCommand(
                        "우주선 탈출",
                        "고장 난 우주선에서 제한 시간 안에 탈출하세요.",
                        "https://example.com/themes/space-escape.jpg"
                )
        );

        //when
        List<ConcurrentResult> results = ConcurrentExecutor.executeConcurrently(100, () -> {
            try {
                themeService.removeThemeById(1L);

                return ConcurrentResult.withSuccess();
            } catch (Throwable e) {
                return ConcurrentResult.withFail(e);
            }
        });

        //then
        assertThat(results).filteredOn(ConcurrentResult::success).hasSize(1);

        assertThat(results).filteredOn(result -> !result.success()).hasSize(99);
        assertThat(results)
                .filteredOn(result -> !result.success())
                .extracting(ConcurrentResult::exception)
                .allMatch(ThemeNotFoundException.class::isInstance);
    }
}
