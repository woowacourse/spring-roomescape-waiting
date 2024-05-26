package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.Theme;
import roomescape.global.handler.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.reservation.ThemeService;
import roomescape.service.reservation.dto.request.ThemeRequest;
import roomescape.service.reservation.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("테마를 저장한다.")
    @Test
    void createTheme() {
        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");

        ThemeResponse theme = themeService.createTheme(themeRequest);

        assertAll(
                () -> assertThat(theme.name()).isEqualTo("happy"),
                () -> assertThat(theme.description()).isEqualTo("hi"),
                () -> assertThat(theme.thumbnail()).isEqualTo("abcd.html")
        );
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAllThemes() {
        themeRepository.save(new Theme("happy", "hi", "abcd.html"));

        List<ThemeResponse> themes = themeService.findAllThemes();

        assertThat(themes).hasSize(1);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteTheme() {
        Theme theme = themeRepository.save(new Theme("happy", "hi", "abcd.html"));

        themeService.deleteTheme(theme.getId());

        List<Theme> themes = themeRepository.findAll();
        assertThat(themes).isEmpty();
    }

    @DisplayName("존재 하지 않는 테마 삭제 테스트")
    @Test
    void deleteNotFoundTheme() {
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @DisplayName("이미 예약된 테마에 대한 삭제 테스트")
    @Test
    void deleteThemeInUsed() {
        LocalDate date = LocalDate.of(2999, 12, 12);
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "happy", "hi", "abcd.html");

        ReservationTime reservedTime = reservationTimeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        reservationRepository.save(new Reservation(
                new Member(1L, "asd", "asd@email.com", "password", Role.USER),
                new Schedule(date, reservedTime, savedTheme)
        ));

        assertThatThrownBy(() -> themeService.deleteTheme(savedTheme.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
