package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
@SqlGroup({
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:truncate.sql"),
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:insert-reservations.sql")
})
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;

    @DisplayName("존재하지 않는 id로 테마를 삭제하면 예외가 발생한다.")
    @Test
    void shouldThrowIllegalArgumentExceptionWhenDeleteWithNonExistId() {
        assertThatCode(() -> themeService.deleteById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_THEME);
    }
}
