package roomescape.theme.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.dto.request.ThemeCreateRequest;
import roomescape.theme.dto.response.ThemeCreateResponse;
import roomescape.theme.entity.Theme;
import roomescape.theme.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeIntegrationTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );

        // when
        var response = themeService.createTheme(request);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo("미소"),
                () -> assertThat(response.description()).isEqualTo("미소 테마"),
                () -> assertThat(response.thumbnail()).isEqualTo("https://miso.com")
        );
    }

    @Test
    @DisplayName("중복되는 테마 이름이 있을 경우 생성할 수 없다.")
    void createThemeWithDuplicateName() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        themeService.createTheme(request1);

        var request2 = new ThemeCreateRequest(
                "미소",
                "미소 테마2",
                "https://miso2.com"
        );

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void getAllThemes() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );
        themeService.createTheme(request1);
        themeService.createTheme(request2);

        // when
        var responses = themeService.getAllThemes();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(2),
                () -> assertThat(responses.getFirst().name()).isEqualTo("미소"),
                () -> assertThat(responses.get(1).name()).isEqualTo("우테코")
        );
    }

    @Test
    @DisplayName("인기 있는 테마를 조회한다.")
    void getPopularThemes() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );
        themeService.createTheme(request1);
        themeService.createTheme(request2);

        // when
        var responses = themeService.getPopularThemes(2);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteTheme() {
        // given
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var theme = themeService.createTheme(request);

        // when
        themeService.deleteTheme(theme.id());

        // then
        assertThat(themeService.getAllThemes()).isEmpty();
    }

    @Test
    @DisplayName("이미 예약 된 예약이 있을 경우 삭제할 수없다.")
    void cantDeleteWhenReserved() {
        //given
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        ThemeCreateResponse response = themeService.createTheme(request);

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = new Theme(response.id(), response.name(), response.description(), response.thumbnail());
        Member member = memberRepository.save(new Member("앤지", "test@test.com", "test", RoleType.USER));
        reservationRepository.save(new Reservation(LocalDate.now(), reservationTime, theme, member));

        //when & then
        assertThatThrownBy(() -> themeService.deleteTheme(response.id()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("해당 테마에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
    }
}
