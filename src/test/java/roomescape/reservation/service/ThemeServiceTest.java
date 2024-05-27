package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.ACTION_THEME;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.JOJO;
import static roomescape.util.Fixture.KAKI;
import static roomescape.util.Fixture.RESERVATION_HOUR_10;
import static roomescape.util.Fixture.TODAY;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Period;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.PopularThemeResponse;
import roomescape.reservation.dto.ThemeSaveRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeService themeService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @DisplayName("중복된 테마 이름을 추가할 수 없다.")
    @Test
    void duplicateThemeNameExceptionTest() {
        ThemeSaveRequest themeSaveRequest = new ThemeSaveRequest(
                HORROR_THEME.getName(),
                HORROR_THEME.getDescription(),
                HORROR_THEME.getThumbnail()
        );
        themeService.save(themeSaveRequest);

        assertThatThrownBy(() -> themeService.save(themeSaveRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    @Test
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("파라미터로 받은 기간 동안에 예약된 상위 n 인기 테마들을 조회한다.")
    @Test
    void findThemesDescOfLastWeekCountOf() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);
        Theme actionTheme = themeRepository.save(ACTION_THEME);

        Member kaki = memberRepository.save(KAKI);
        Member jojo = memberRepository.save(JOJO);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(kaki, TODAY, actionTheme, hour10, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(jojo, TODAY, actionTheme, hour10, ReservationStatus.SUCCESS));

        List<PopularThemeResponse> popularThemeResponses = themeService.findPopularThemesBetweenPeriod(Period.DAY, 2);

        assertAll(
                () -> assertThat(popularThemeResponses.get(0).name()).isEqualTo(actionTheme.getName()),
                () -> assertThat(popularThemeResponses).hasSize(2)
        );
    }

    @DisplayName("이미 해당 테마로 예약 되있을 경우 삭제 시 예외가 발생한다.")
    @Test
    void deleteExceptionTest() {
        ReservationTime hour10 = reservationTimeRepository.save(RESERVATION_HOUR_10);

        Theme horrorTheme = themeRepository.save(HORROR_THEME);

        Member kaki = memberRepository.save(KAKI);

        reservationRepository.save(new Reservation(kaki, TODAY, horrorTheme, hour10, ReservationStatus.SUCCESS));

        assertThatThrownBy(() -> themeService.delete(horrorTheme.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
