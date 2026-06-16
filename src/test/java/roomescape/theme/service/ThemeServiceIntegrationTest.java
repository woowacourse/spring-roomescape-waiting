package roomescape.theme.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.slot.domain.ReservationSlot;
import roomescape.support.ServiceSupport;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.fixture.ThemeFixture;
import roomescape.time.domain.ReservationTime;
import roomescape.time.fixture.ReservationTimeFixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorInformation.THEME_NOT_FOUND;

@Import(ThemeService.class)
class ThemeServiceIntegrationTest extends ServiceSupport {

    @Autowired
    private ThemeService themeService;

    @Test
    @DisplayName("등록된 테마가 여러개이면 조회 시 등록된 갯수만큼 반환한다.")
    void readThemes() {
        // given
        List<Theme> themes = List.of(
                ThemeFixture.theme("테마1"),
                ThemeFixture.theme("테마2"),
                ThemeFixture.theme("테마3")
        );
        saveAll(themes);

        // when
        List<Theme> actual = themeService.readThemes();

        // then
        assertThat(actual).hasSize(themes.size());
    }

    @Test
    @DisplayName("등록된 테마와 조회되는 테마의 모든 필드가 일치한다.")
    void readTheme() {
        // given
        Theme savedTheme = saveTheme(ThemeFixture.theme());

        // when
        Theme actual = themeService.readTheme(savedTheme.getId());

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("등록되지 않은 테마 조회 시 예외가 발생한다.")
    void readTheme_unregistered() {
        // given
        Long unregisteredId = Long.MIN_VALUE;

        // when & then
        assertThatThrownBy(() -> themeService.readTheme(unregisteredId))
                .isInstanceOf(ThemeException.class)
                .hasMessage(THEME_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("활성화된 테마 목록을 가나다순으로 조회한다.")
    void readActiveThemes() {
        // given
        List<Theme> themes = saveAll(List.of(
                ThemeFixture.activeTheme("다테마"),
                ThemeFixture.activeTheme("나테마"),
                ThemeFixture.activeTheme("가테마"))
        );
        Collections.sort(themes, Comparator.comparing(Theme::getName));

        // when
        List<Theme> actual = themeService.readActiveThemes();

        // then
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(themes);
    }

    @Test
    @DisplayName("테마를 1개 등록하면 테마 데이터 수가 1 증가한다.")
    void register() {
        // given
        String name = "테마1";
        String description = "테마1 설명";
        String thumbnail = "테마1 썸네일";
        Long amount = 1000L;

        // when
        themeService.register(name, description, thumbnail, amount);

        // then
        assertThat(themeService.readThemes())
                .hasSize(1);
    }

    @Test
    @DisplayName("등록한 테마와 다시 조회한 테마의 모든 필드가 일치한다.")
    void register_theme_fields_match() {
        // given
        String name = "테마1";
        String description = "테마1 설명";
        String thumbnail = "테마1 썸네일";
        Long amount = 1000L;

        // when
        Theme registeredTheme = themeService.register(name, description, thumbnail, amount);

        // then
        assertThat(registeredTheme)
                .usingRecursiveComparison()
                .isEqualTo(themeService.readTheme(registeredTheme.getId()));
    }

    @Test
    @DisplayName("테마를 활성화한다.")
    void updateStatus_active() {
        // given
        Theme savedTheme = saveTheme(ThemeFixture.theme());

        // when
        themeService.updateStatus(savedTheme.getId(), true);

        // then
        assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isTrue();
    }

    @Test
    @DisplayName("테마를 비활성화한다.")
    void updateStatus_deactivate() {
        // given
        Theme savedTheme = saveTheme(ThemeFixture.activeTheme());

        // when
        themeService.updateStatus(savedTheme.getId(), false);

        // then
        assertThat(themeRepository.findById(savedTheme.getId()).get().isActive())
                .isFalse();
    }

    @Test
    @DisplayName("슬롯에 등록된 활성화 테마를 조회한다.")
    void readSlotOfThemes() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        saveSlot(ReservationSlot.of(date, time, theme));

        // when
        List<Theme> actual = themeService.readSlotOfThemes();

        // then
        assertThat(actual)
                .hasSize(1);

        assertThat(actual.get(0))
                .usingRecursiveComparison()
                .isEqualTo(theme);
    }

    @Test
    @DisplayName("슬롯이 없는 테마는 슬롯 테마 조회 시 반환되지 않는다.")
    void readSlotOfThemes_without_slot() {
        // given
        saveTheme(ThemeFixture.activeTheme());

        // when
        List<Theme> actual = themeService.readSlotOfThemes();

        // then
        assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("비활성화 테마는 슬롯이 있어도 슬롯 테마 조회 시 반환되지 않는다.")
    void readSlotOfThemes_inactive_theme_not_returned() {
        // given
        Theme inactiveTheme = saveTheme(ThemeFixture.inActiveTheme());
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time = saveTime(ReservationTimeFixture.activeTime15());
        saveSlot(ReservationSlot.of(date, time, inactiveTheme));

        // when
        List<Theme> actual = themeService.readSlotOfThemes();

        // then
        assertThat(actual)
                .isEmpty();
    }

    @Test
    @DisplayName("여러 슬롯이 등록된 테마는 슬롯 테마 조회 시 한 번만 반환된다.")
    void readSlotOfThemes_multiple_slots_distinct() {
        // given
        Theme theme = saveTheme(ThemeFixture.activeTheme());
        ReservationDate date = saveDate(ReservationDateFixture.activeOneWeekLater());
        ReservationTime time1 = saveTime(ReservationTimeFixture.activeTime15());
        ReservationTime time2 = saveTime(ReservationTimeFixture.activeTime16());
        saveSlot(ReservationSlot.of(date, time1, theme));
        saveSlot(ReservationSlot.of(date, time2, theme));

        // when
        List<Theme> actual = themeService.readSlotOfThemes();

        // then
        assertThat(actual)
                .hasSize(1);
    }

    private List<Theme> saveAll(List<Theme> themes) {
        List<Theme> savedThemes = new ArrayList<>();
        for (Theme theme : themes) {
            savedThemes.add(saveTheme(theme));
        }
        return savedThemes;
    }

}
