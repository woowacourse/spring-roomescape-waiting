package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private ReservationDbFixture reservationDbFixture;
    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private CleanUp cleanUp;

    @BeforeEach
    void setUp() {
        cleanUp.all();
    }

    @Test
    void 테마를_생성한다() {
        ThemeCreateRequest request = new ThemeCreateRequest("공포", "공포 테마", "공포.jpg");

        ThemeResponse response = themeService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("공포");
        assertThat(response.description()).isEqualTo("공포 테마");
        assertThat(response.thumbnail()).isEqualTo("공포.jpg");
    }

    @Test
    void 테마를_조회한다() {
        Theme theme = themeDbFixture.공포();

        ThemeResponse response = themeService.getAll().get(0);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(theme.getName());
        assertThat(response.description()).isEqualTo(theme.getDescription());
        assertThat(response.thumbnail()).isEqualTo(theme.getThumbnail());

    }

    @Test
    void 테마를_삭제한다() {
        Theme theme = themeDbFixture.공포();

        themeService.deleteById(theme.getId());

        assertThat(themeService.getAll()).isEmpty();
    }

    @Test
    void 이미_해당_테마의_예약이_존재한다면_삭제할_수_없다() {
        Reservation reservation = reservationDbFixture.예약_유저1_내일_10시_공포();

        assertThatThrownBy(() -> themeService.deleteById(reservation.getTheme().getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void 존재하지_않는_테마를_삭제할_수_없다() {
        assertThatThrownBy(() -> themeService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("[ERROR] 해당 테마가 존재하지 않습니다.");
    }

    @Test
    void 지난_일주일_간_인기_테마_10개를_조회한다() {
        List<Theme> themes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            themes.add(themeDbFixture.커스텀_테마("테마" + i));
        }

        for (int i = 0; i < 20; i++) {
            addReservation(19 - i, ReservationDateFixture.예약날짜_오늘, reservationTimeDbFixture.열시(), themes.get(i));
            addReservation(i, ReservationDateFixture.예약날짜_7일전, reservationTimeDbFixture.열시(), themes.get(i));
        }

        List<ThemeResponse> popularThemes = themeService.getPopularThemes();

        assertThat(popularThemes)
                .hasSize(10)
                .extracting(ThemeResponse::name)
                .containsExactlyInAnyOrder(
                        "테마0", "테마1", "테마2", "테마3", "테마4",
                        "테마5", "테마6", "테마7", "테마8", "테마9"
                );
    }

    private void addReservation(int count, ReservationDate date, ReservationTime time, Theme theme) {
        for (int i = 0; i < count; i++) {
            reservationDbFixture.예약_유저1(date, time, theme);
        }
    }
}
