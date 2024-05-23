package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.defaultMember;
import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;
import roomescape.exception.RoomescapeException;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest extends FixtureUsingTest {

    @Autowired
    private ThemeService themeService;

    @DisplayName("테마를 모두 조회할 수 있다.")
    @Test
    void findAllTest() {
        //when
        List<ThemeResponse> themeResponses = themeService.findAll();

        //then
        assertThat(themeResponses).hasSize(countOfSavedTheme);
    }

    @DisplayName("예약 개수에 따라 인기 테마를 조회할 수 있다.")
    @Test
    void findPopularTest() {
        //when
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 30));
        reservationTime = reservationTimeRepository.save(reservationTime);

        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(1), reservationTime, theme1, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(2), reservationTime, theme1, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(3), reservationTime, theme1, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(4), reservationTime, theme1, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(5), reservationTime, theme1, USER1));

        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(1), reservationTime, theme3, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(2), reservationTime, theme3, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(3), reservationTime, theme3, USER1));

        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(1), reservationTime, theme2, USER1));
        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(3), reservationTime, theme2, USER1));

        reservationRepository.save(
                new Reservation(LocalDate.now().minusDays(3), reservationTime, theme4, USER1));

        //when
        List<ThemeResponse> popularThemes = themeService.findAndOrderByPopularity(5);

        assertThat(popularThemes).contains(
                ThemeResponse.from(theme1),
                ThemeResponse.from(theme3),
                ThemeResponse.from(theme2),
                ThemeResponse.from(theme4)
        );
    }

    @DisplayName("동일한 이름의 테마를 예약할 수 없다.")
    @Test
    void duplicatedThemeSaveFailTest() {
        assertThatThrownBy(() -> themeService.save(new ThemeRequest(
                theme1.getName(), "description", "thumbnail"
        ))).isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_THEME.getMessage());
    }

    @DisplayName("다른 이름의 테마를 예약할 수 있다.")
    @Test
    void notDuplicatedThemeNameSaveTest() {
        themeService.save(new ThemeRequest("otherName", "description", "thumbnail"));

        assertThat(themeRepository.findAll())
                .hasSize(countOfSavedTheme + 1);
    }

    @DisplayName("테마에 예약이 없다면 테마를 삭제할 수 있다.")
    @Test
    void removeSuccessTest() {
        themeService.delete(theme1.getId());
        assertThat(themeRepository.findById(theme1.getId())).isEmpty();
    }

    @DisplayName("테마에 예약이 있다면 테마를 삭제할 수 없다.")
    @Test
    void removeFailTest() {
        //given
        reservationRepository.save(
                new Reservation(LocalDate.now().plusDays(1), reservationTime_10_0, theme1, defaultMember));

        //when & then
        assertThatThrownBy(() -> themeService.delete(theme1.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_THEME.getMessage());
    }

    @DisplayName("존재하지 않는 테마 id로 삭제하더라도 오류로 간주하지 않는다.")
    @Test
    void notExistThemeDeleteTest() {
        assertThatCode(() -> themeService.delete(themeIdNotSaved))
                .doesNotThrowAnyException();
    }
}
