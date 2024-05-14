package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.ServiceTest;
import roomescape.global.exception.NotFoundException;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static roomescape.TestFixture.*;
import static roomescape.TestFixture.WOOTECO_THEME;

class ThemeServiceTest extends ServiceTest {
    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    void create() {
        // given
        Theme theme = WOOTECO_THEME();

        // when
        Theme createdTheme = themeService.create(theme);

        // then
        assertThat(createdTheme.getId()).isNotNull();
    }

    @Test
    @DisplayName("테마 목록을 조회한다.")
    void findAll() {
        // given
        themeService.create(WOOTECO_THEME());
        themeService.create(HORROR_THEME());

        // when
        List<Theme> themes = themeService.findAll();

        // then
        assertThat(themes).hasSize(2)
                .extracting(Theme::getName)
                .contains(WOOTECO_THEME_NAME, HORROR_THEME_NAME);
    }

    @Test
    @DisplayName("Id로 조회하려는 테마가 존재하지 않는 경우 예외가 발생한다.")
    void findByNotExistId() {
        // given
        Long notExistingId = 100L;

        // when & then
        assertThatThrownBy(() -> themeService.findById(notExistingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteById() {
        // given
        Long themeId = themeService.create(WOOTECO_THEME()).getId();

        // when & then
        assertThatCode(() -> themeService.deleteById(themeId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("최근 일주일을 기준으로 예약이 많은 순으로 테마 10개 목록을 조회한다.")
    void findAllPopular() {
        // given
        Theme secondRankTheme = themeService.create(HORROR_THEME());
        Theme firstRankTheme = themeService.create(WOOTECO_THEME());

        ReservationTime reservationTime = reservationTimeService.create(new ReservationTime(MIA_RESERVATION_TIME));
        Member member = memberService.create(USER_MIA());
        reservationRepository.save(new Reservation(
                member, LocalDate.now().minusDays(1), reservationTime, firstRankTheme));

        // when
        List<Theme> themes = themeService.findAllPopular();

        // then
        assertThat(themes).hasSize(2)
                .extracting(Theme::getId)
                .containsExactly(firstRankTheme.getId(), secondRankTheme.getId());
    }
}
