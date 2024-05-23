package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.JOJO_PASSWORD;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
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
        ThemeSaveRequest themeSaveRequest = new ThemeSaveRequest(HORROR_THEME_NAME, HORROR_DESCRIPTION, THUMBNAIL);
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

    @DisplayName("일주일 내에 예약된 상위 n 인기 테마들을 조회한다.")
    @Test
    void findThemesDescOfLastWeekCountOf() {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        Theme theme1 = themeRepository.save(
                new Theme(
                        new ThemeName(HORROR_THEME_NAME),
                        new Description(HORROR_DESCRIPTION),
                        THUMBNAIL
                )
        );

        Theme theme2 = themeRepository.save(
                new Theme(
                        new ThemeName("액션"),
                        new Description("액션 탈출"),
                        THUMBNAIL
                )
        );

        Member kaki = memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));
        Member jojo = memberRepository.save(Member.createMemberByUserRole(new MemberName(JOJO_NAME), JOJO_EMAIL, JOJO_PASSWORD));

        reservationRepository.save(new Reservation(kaki, TODAY, theme1, reservationTime, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(kaki, TODAY, theme2, reservationTime, ReservationStatus.SUCCESS));
        reservationRepository.save(new Reservation(jojo, TODAY, theme2, reservationTime, ReservationStatus.SUCCESS));

        List<PopularThemeResponse> popularThemeResponses = themeService.findThemesDescOfLastWeekTopOf(2);
        assertAll(
                () -> assertThat(popularThemeResponses.get(0).name()).isEqualTo("액션"),
                () -> assertThat(popularThemeResponses).hasSize(2)
        );
    }

    @DisplayName("이미 해당 테마로 예약 되있을 경우 삭제 시 예외가 발생한다.")
    @Test
    void deleteExceptionTest() {
        Theme theme = themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime hour10 = reservationTimeRepository.save(new ReservationTime(HOUR_10));

        Member member = memberRepository.save(
                Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        reservationRepository.save(new Reservation(member, TODAY, theme, hour10, ReservationStatus.SUCCESS));

        assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
