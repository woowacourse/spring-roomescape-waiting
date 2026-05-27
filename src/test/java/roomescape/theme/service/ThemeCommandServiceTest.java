package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.fixture.ThemeFixture;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.support.ServiceTest;
import roomescape.support.TestDataHelper;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.application.service.ThemeCommandService;

@ServiceTest
@Transactional
public class ThemeCommandServiceTest {

    @Autowired
    private ThemeCommandService themeCommandService;

    @Autowired
    private TestDataHelper testHelper;

    @DisplayName("테마의 정상 추가를 테스트합니다.")
    @Test
    void save_theme_successfully() {
        ThemeResult result = themeCommandService.save(ThemeFixture.horrorThemeCreateCommand());

        assertThat(result.id()).isPositive();
        assertThat(result).isEqualTo(ThemeFixture.horrorThemeQueryResult(result.id()));
    }

    @DisplayName("중복된 테마 추가 시 예외 발생을 테스트합니다.")
    @Test
    void save_duplicated_theme_exception() {
        testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());

        assertThatThrownBy(() -> themeCommandService.save(ThemeFixture.horrorThemeCreateCommand()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이름과 설명이 같은 테마가 이미 존재합니다.");
    }

    @DisplayName("테마의 삭제를 테스트합니다.")
    @Test
    void delete_theme() {
        Long themeId = testHelper.insertTheme(ThemeFixture.horrorThemeCreateCommand());

        assertThatNoException().isThrownBy(() -> themeCommandService.delete(themeId));
    }

    @DisplayName("삭제할 테마가 없을 시 예외 발생을 테스트합니다.")
    @Test
    void delete_not_found_theme_exception() {
        assertThatThrownBy(() -> themeCommandService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @DisplayName("테마 삭제 시 해당 테마를 사용한 예약이 존재하면 예외 발생을 테스트합니다.")
    @Test
    void delete_theme_with_existing_reservation_exception() {
        Long themeId = testHelper.insertTheme("테마1", "설명1", "img1.jpg");
        Long timeId = testHelper.insertReservationTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2026, 5, 10);
        testHelper.insertReservation(
                "스타크",
                date,
                themeId,
                timeId
        );

        assertThatThrownBy(() -> themeCommandService.delete(themeId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
    }
}
