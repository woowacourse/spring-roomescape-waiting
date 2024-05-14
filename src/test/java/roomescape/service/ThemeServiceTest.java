package roomescape.service;

import static java.time.Month.FEBRUARY;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.policy.FixedDateWeeklyRankingPolicy;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.web.dto.response.theme.ThemeResponse;

@SpringBootTest
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        themeRepository.deleteAll();
        reservationTimeRepository.deleteAll();
    }

    @Test
    @DisplayName("랭킹 순으로 조회할 수 있다")
    void findAllPopularTheme_ShouldReturnTrendingThemes() {
        // given
        Theme savedTheme1 = themeRepository.save(new Theme("name1", "description", "thumbnail"));
        Theme savedTheme2 = themeRepository.save(new Theme("name2", "description", "thumbnail"));
        Theme savedTheme3 = themeRepository.save(new Theme("name3", "description", "thumbnail"));

        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 0)));

        creatReservation(1, savedReservationTime, savedTheme1);
        creatReservation(2, savedReservationTime, savedTheme1);
        creatReservation(3, savedReservationTime, savedTheme1);

        creatReservation(1, savedReservationTime, savedTheme2);
        creatReservation(2, savedReservationTime, savedTheme2);

        creatReservation(1, savedReservationTime, savedTheme3);

        // when
        List<ThemeResponse> popularTheme = themeService.findAllPopularTheme(new FixedDateWeeklyRankingPolicy());

        // then
        Assertions.assertThat(popularTheme)
                .hasSize(3)
                .extracting("name")
                .containsExactly("name1", "name2", "name3");
    }

    @Test
    @DisplayName("랭킹은 최대 10개까지 조회할 수 있다")
    void findAllPopularTheme_ShouldReturnMax10TrendingThemes() {
        // given
        Theme savedTheme1 = themeRepository.save(new Theme("name1", "description", "thumbnail"));
        Theme savedTheme2 = themeRepository.save(new Theme("name2", "description", "thumbnail"));
        Theme savedTheme3 = themeRepository.save(new Theme("name3", "description", "thumbnail"));
        Theme savedTheme4 = themeRepository.save(new Theme("name4", "description", "thumbnail"));
        Theme savedTheme5 = themeRepository.save(new Theme("name5", "description", "thumbnail"));
        Theme savedTheme6 = themeRepository.save(new Theme("name6", "description", "thumbnail"));
        Theme savedTheme7 = themeRepository.save(new Theme("name7", "description", "thumbnail"));
        Theme savedTheme8 = themeRepository.save(new Theme("name8", "description", "thumbnail"));
        Theme savedTheme9 = themeRepository.save(new Theme("name9", "description", "thumbnail"));
        Theme savedTheme10 = themeRepository.save(new Theme("name10", "description", "thumbnail"));
        Theme savedTheme11 = themeRepository.save(new Theme("name11", "description", "thumbnail"));

        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(1, 0)));

        creatReservation(1, savedReservationTime, savedTheme1);
        creatReservation(1, savedReservationTime, savedTheme2);
        creatReservation(1, savedReservationTime, savedTheme3);
        creatReservation(1, savedReservationTime, savedTheme4);
        creatReservation(1, savedReservationTime, savedTheme5);
        creatReservation(1, savedReservationTime, savedTheme6);
        creatReservation(1, savedReservationTime, savedTheme7);
        creatReservation(1, savedReservationTime, savedTheme8);
        creatReservation(1, savedReservationTime, savedTheme9);
        creatReservation(1, savedReservationTime, savedTheme10);
        creatReservation(1, savedReservationTime, savedTheme11);

        // when
        List<ThemeResponse> popularTheme = themeService.findAllPopularTheme(new FixedDateWeeklyRankingPolicy());

        // then
        Assertions.assertThat(popularTheme)
                .hasSize(10);
    }

    private void creatReservation(int day, ReservationTime reservationTime, Theme theme) {
        reservationRepository.save(
                new Reservation(LocalDate.of(2023, FEBRUARY, day), reservationTime, theme, new Member("a", "b", "C")));
    }
}
