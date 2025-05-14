package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForeignKeyException;
import roomescape.common.exception.InvalidIdException;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationDaoImpl;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.dao.ThemeDaoImpl;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.dto.ThemeRequest;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    private ThemeDao themeDao;
    private ReservationDao reservationDao;
    private ThemeRepository themeRepository;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeDao = mock(ThemeDaoImpl.class);
        reservationDao = mock(ReservationDaoImpl.class);
        themeRepository = mock(ThemeRepository.class);

        themeService = new ThemeService(reservationDao, themeRepository, themeDao);
    }

    @DisplayName("테마 내역을 조회하는 기능을 구현한다")
    @Test
    void findAll() {
        Theme theme = new Theme(1L, "name1", "description1", "thumbnail1");
        when(themeRepository.findAll()).thenReturn(List.of(theme));

        assertThat(themeService.findAll()).hasSize(1);
        verify(themeRepository, times(1)).findAll();
    }

    @DisplayName("기간 동안의 순위권에 든 테마 내역을 조회하는 기능을 구현한다")
    @Test
    void findRankedByPeriod() {
        Theme theme = new Theme(1L, "name1", "description1", "thumbnail1");
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);
        when(themeDao.findRankedByPeriod(startDate, endDate, 10)).thenReturn(List.of(theme));

        assertThat(themeService.findRankedByPeriod()).hasSize(1);
        verify(themeDao, times(1)).findRankedByPeriod(startDate, endDate, 10);
    }

    @DisplayName("이미 존재하는 테마를 다시 추가하려는 경우 예외를 발생시킨다")
    @Test
    void exception_add_duplicate_theme() {
        when(themeRepository.existsByName("name1")).thenReturn(true);

        ThemeRequest themeRequest = new ThemeRequest("name1", "description2", "thumbnail2");
        assertThatThrownBy(() -> themeService.add(themeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @DisplayName("테마 아이디가 존재하지 않는 경우 예외를 발생시킨다")
    @Test
    void exception_delete_invalid_themeId() {
        when(themeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("이미 예약된 테마 아이디를 삭제하려는 경우 예외를 발생시킨다")
    @Test
    void exception_delete_reserved_themeId() {
        Theme theme = new Theme(1L, "name1", "description1", "thumbnail1");
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(themeRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> themeService.deleteById(1L))
                .isInstanceOf(ForeignKeyException.class);
    }
}
