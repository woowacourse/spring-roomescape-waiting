package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationSlotDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;
import roomescape.dto.command.ThemeCommand;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeDao timeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationSlotDao reservationSlotDao;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 테마를_추가한다() {
        // given
        ThemeCommand command = new ThemeCommand("방탈출1", "설명", "https://thumb.com");

        // when
        ThemeResponse response = themeService.addTheme(command);

        // then
        assertThat(response.name()).isEqualTo("방탈출1");
    }

    @Test
    void 이미_존재하는_테마명으로_추가하면_예외가_발생한다() {
        // given
        ThemeCommand command = new ThemeCommand("방탈출1", "설명2", "https://thumb2.com");
        saveTheme("방탈출1", "설명", "https://thumb.com");

        // when & then
        assertThatThrownBy(() -> themeService.addTheme(command))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 전체_테마를_조회한다() {
        // given
        saveTheme("방탈출1", "설명1", "https://thumb1.com");
        saveTheme("방탈출2", "설명2", "https://thumb2.com");

        // when
        List<ThemeResponse> responses = themeService.getThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("name").containsExactly("방탈출1", "방탈출2");
    }

    @Test
    void 인기_테마를_조회한다() {
        Theme popularTheme = saveTheme("공포의 저택", "설명", "https://thumb.com");
        Theme normalTheme = saveTheme("사라진 연구소", "설명", "https://thumb2.com");

        LocalDate today = LocalDate.now();
        ReservationTime time = timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        saveReservation("예약자", today.minusDays(1), time, popularTheme);
        saveReservation("예약자", today.minusDays(2), time, popularTheme);
        saveReservation("예약자", today.minusDays(3), time, popularTheme);
        saveReservation("예약자", today.minusDays(1), time, normalTheme);

        List<ThemeResponse> responses = themeService.getPopularThemes(today);

        assertThat(responses.get(0).name()).isEqualTo("공포의 저택");
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme saved = saveTheme("방탈출1", "설명", "https://thumb.com");

        // when & then
        assertThatNoException().isThrownBy(() -> themeService.deleteTheme(saved.getId()));
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 예약에_존재하는_테마를_삭제하면_예외가_발생한다() {
        // given
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationTime time = timeDao.insert(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        saveReservation("브라운", LocalDate.of(2026, 5, 5), time, theme);

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId()))
                .isInstanceOf(RoomEscapeException.class);
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeDao.insert(Theme.createWithoutId(name, description, thumbnail));
    }

    private ReservationSlot saveSlot(LocalDate date, ReservationTime time, Theme theme) {
        return reservationSlotDao.findOrCreate(new ReservationSlot(date, time, theme));
    }

    private void saveReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        ReservationSlot slot = saveSlot(date, time, theme);
        reservationDao.insert(Reservation.createWithoutId(name, slot));
    }
}
