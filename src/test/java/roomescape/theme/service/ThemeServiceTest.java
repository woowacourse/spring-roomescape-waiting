package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.MEMBER;
import static roomescape.fixture.ThemeFixture.THEME;
import static roomescape.fixture.TimeFixture.TIME;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.fake.FakeThemeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.CreateThemeRequest;
import roomescape.theme.dto.ThemeResponse;

class ThemeServiceTest {

    FakeThemeDao fakeThemeDao = new FakeThemeDao();
    FakeReservationDao fakeReservationDao = new FakeReservationDao();
    FakeReservationTimeDao fakeReservationTimeDao = new FakeReservationTimeDao();
    ThemeService themeService = new ThemeService(fakeThemeDao, fakeReservationDao);

    @DisplayName("저장할 테마 이름이 중복될 경우 예외가 발생한다.")
    @Test
    void testValidateNameDuplication() {
        // given
        CreateThemeRequest request1 = new CreateThemeRequest("woooteco", "우테코를 탈출하라", "https://www.woowacourse.io/");
        CreateThemeRequest request2 = new CreateThemeRequest("woooteco", "우테코에서 살아남기", "https://www.woowacourse2.io/");
        themeService.createTheme(request1);
        // when
        // then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 이름의 테마는 이미 존재합니다.");
    }

    @DisplayName("테마를 저장할 수 있다.")
    @Test
    void testCreate() {
        // given
        CreateThemeRequest request = new CreateThemeRequest("woooteco", "우테코를 탈출하라", "https://www.woowacourse.io/");
        // when
        ThemeResponse result = themeService.createTheme(request);
        // then
        Theme saved = fakeThemeDao.findById(result.id()).orElseThrow();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.name()).isEqualTo(request.name()),
                () -> assertThat(result.description()).isEqualTo(request.description()),
                () -> assertThat(result.thumbnail()).isEqualTo(request.thumbnail()),
                () -> assertThat(saved.getName()).isEqualTo(request.name()),
                () -> assertThat(saved.getDescription()).isEqualTo(request.description()),
                () -> assertThat(saved.getThumbnail()).isEqualTo(request.thumbnail())
        );
    }

    @DisplayName("테마 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        CreateThemeRequest request1 = new CreateThemeRequest("woooteco1", "우테코를 탈출하라", "https://www.woowacourse.io/");
        CreateThemeRequest request2 = new CreateThemeRequest("woooteco2", "우테코에 합격하라", "https://www.woowacourse.io/");
        themeService.createTheme(request1);
        themeService.createTheme(request2);
        // when
        List<ThemeResponse> result = themeService.findAll();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약이 존재하는 테마를 삭제할 경우 예외가 발생한다.")
    @Test
    void testIllegalDelete() {
        // given
        fakeReservationDao.save(Reservation.register(MEMBER, LocalDate.now().plusDays(1), TIME, THEME));
        // when
        // then
        assertThatThrownBy(() -> themeService.deleteThemeById(THEME.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 테마는 삭제할 수 없습니다.");
    }

    @DisplayName("테마를 삭제할 수 있다.")
    @Test
    void testDelete() {
        // given
        Theme theme = new Theme(null, "우테코방탈출", "탈출탈출탈출", "포비솔라브라운");
        Theme savedTheme = fakeThemeDao.save(theme);
        // when
        themeService.deleteThemeById(savedTheme.getId());
        // then
        List<Theme> themes = fakeThemeDao.findAll();
        assertThat(themes).isEmpty();
    }
}
