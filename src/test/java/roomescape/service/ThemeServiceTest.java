package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import roomescape.controller.dto.ThemeRequest;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.repository.ScheduleDao;
import roomescape.repository.ThemeDao;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeDao themeDao;

    @Mock
    private ScheduleDao scheduleDao;

    @InjectMocks
    private ThemeService themeService;

    @DisplayName("중복 이름이 아니면 테마를 저장한다.")
    @Test
    void saveTheme() {
        ThemeRequest request = new ThemeRequest("잠긴 방", "설명", "https://example.com/theme.jpg", 20000);
        given(themeDao.existsByName("잠긴 방")).willReturn(false);
        given(themeDao.save(request.name(), request.description(), request.thumbnailUrl(), request.price())).willReturn(1L);

        Long themeId = themeService.saveTheme(request);

        assertThat(themeId).isEqualTo(1L);
    }

    @DisplayName("이미 존재하는 이름이면 테마를 저장하지 않는다.")
    @Test
    void saveDuplicateTheme() {
        ThemeRequest request = new ThemeRequest("잠긴 방", "설명", "https://example.com/theme.jpg", 20000);
        given(themeDao.existsByName("잠긴 방")).willReturn(true);

        assertThatThrownBy(() -> themeService.saveTheme(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_THEME_NAME);

        verify(themeDao, never()).save(any(), any(), any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @DisplayName("DB 유니크 제약 예외도 도메인 예외로 변환한다.")
    @Test
    void saveThemeDuplicateKeyException() {
        ThemeRequest request = new ThemeRequest("잠긴 방", "설명", "https://example.com/theme.jpg", 20000);
        given(themeDao.existsByName("잠긴 방")).willReturn(false);
        given(themeDao.save(request.name(), request.description(), request.thumbnailUrl(), request.price()))
                .willThrow(new DuplicateKeyException("duplicate"));

        assertThatThrownBy(() -> themeService.saveTheme(request))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.DUPLICATE_THEME_NAME);
    }

    @DisplayName("스케줄이 참조하는 테마는 삭제할 수 없다.")
    @Test
    void deleteThemeReferencedBySchedule() {
        given(scheduleDao.existsByThemeId(1L)).willReturn(true);

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.REFERENTIAL_INTEGRITY);

        verify(themeDao, never()).delete(1L);
    }

    @DisplayName("삭제 중 무결성 예외가 발생하면 도메인 예외로 변환한다.")
    @Test
    void deleteThemeDataIntegrityViolationException() {
        given(scheduleDao.existsByThemeId(1L)).willReturn(false);
        givenThemeDeleteFails(1L);

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(DomainErrorCode.REFERENTIAL_INTEGRITY);
    }

    @DisplayName("테마 목록을 조회한다.")
    @Test
    void findAll() {
        given(themeDao.findAll()).willReturn(List.of(
                new Theme(1L, "잠긴 방", "설명", "https://example.com/theme.jpg", 20000)
        ));

        List<Theme> themes = themeService.findAll();

        assertThat(themes).hasSize(1);
        assertThat(themes.get(0).getId()).isEqualTo(1L);
        assertThat(themes.get(0).getName()).isEqualTo("잠긴 방");
    }

    @DisplayName("인기 테마는 최근 7일의 RESERVED 예약 기준으로 조회한다.")
    @Test
    void findPopularThemes() {
        given(themeDao.findPopularThemes(
                any(LocalDate.class),
                any(LocalDate.class),
                eq(ReservationStatus.RESERVED),
                eq(10)
        )).willReturn(List.of(new Theme(1L, "인기 테마", "설명", "https://example.com/theme.jpg", 20000)));

        List<Theme> themes = themeService.findPopularThemes();

        assertThat(themes).hasSize(1);
        assertThat(themes.get(0).getName()).isEqualTo("인기 테마");
    }

    private void givenThemeDeleteFails(long id) {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("referenced"))
                .when(themeDao)
                .delete(id);
    }
}
