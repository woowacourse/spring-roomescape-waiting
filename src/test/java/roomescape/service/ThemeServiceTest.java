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
import org.springframework.http.HttpStatus;
import roomescape.DatabaseInitializer;
import roomescape.common.exception.RoomEscapeException;
import roomescape.repository.ReservationRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.repository.ReservationTimeRepository;
import roomescape.domain.ReservationTime;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Theme;
import roomescape.dto.command.CreateThemeCommand;
import roomescape.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeRepository timeDao;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        databaseInitializer.clear();
    }

    @Test
    void 테마를_추가한다() {
        // given
        CreateThemeCommand command = new CreateThemeCommand("방탈출1", "설명", "https://thumb.com");

        // when
        ThemeResponse response = themeService.createTheme(command);

        // then
        assertThat(response.name()).isEqualTo("방탈출1");
    }

    @Test
    void 이미_존재하는_테마명으로_추가하면_409를_반환한다() {
        // given
        CreateThemeCommand command = new CreateThemeCommand("방탈출1", "설명2", "https://thumb2.com");
        saveTheme("방탈출1", "설명", "https://thumb.com");

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(command))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
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
        ReservationTime time = timeDao.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

        reservationRepository.save(Reservation.createWithoutId("예약자", new ReservationSlot(today.minusDays(1), time, popularTheme)));
        reservationRepository.save(Reservation.createWithoutId("예약자", new ReservationSlot(today.minusDays(2), time, popularTheme)));
        reservationRepository.save(Reservation.createWithoutId("예약자", new ReservationSlot(today.minusDays(3), time, popularTheme)));
        reservationRepository.save(Reservation.createWithoutId("예약자", new ReservationSlot(today.minusDays(1), time, normalTheme)));

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
    void 존재하지_않는_테마를_삭제하면_404를_반환한다() {
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void 예약에_존재하는_테마를_삭제하면_409를_반환한다() {
        // given
        Theme theme = saveTheme("방탈출1", "설명", "https://thumb.com");
        ReservationTime time = timeDao.save(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        reservationRepository.save(Reservation.createWithoutId("브라운", new ReservationSlot(LocalDate.of(2026, 5, 5), time, theme)));

        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(theme.getId()))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(exception -> assertThat(((RoomEscapeException) exception).getErrorCode().getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    private Theme saveTheme(String name, String description, String thumbnail) {
        return themeRepository.save(Theme.createWithoutId(name, description, thumbnail));
    }
}
