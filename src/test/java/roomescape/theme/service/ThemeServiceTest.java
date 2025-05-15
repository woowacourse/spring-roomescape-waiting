package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.CleanUp;
import roomescape.fixture.MemberDbFixture;
import roomescape.fixture.ReservationDateFixture;
import roomescape.fixture.ReservationDateTimeDbFixture;
import roomescape.fixture.ReservationTimeDbFixture;
import roomescape.fixture.ThemeDbFixture;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NoElementsException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.controller.request.ThemeCreateRequest;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;
    @Autowired
    private ThemeDbFixture themeDbFixture;
    @Autowired
    private ThemeService themeService;
    @Autowired
    private MemberDbFixture memberDbFixture;
    @Autowired
    private ReservationDateTimeDbFixture reservationDateTimeDbFixture;
    @Autowired
    private ReservationRepository reservationRepository;
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
        Theme theme = themeDbFixture.공포();
        Member reserver = memberDbFixture.유저1_생성();
        ReservationDateTime reservationDateTime = reservationDateTimeDbFixture.내일_열시();
        Reservation reservation = Reservation.reserve(
                reserver, reservationDateTime, theme
        );
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> themeService.deleteById(reservation.getTheme().getId()))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
    }

    @Test
    void 존재하지_않는_테마를_삭제할_수_없다() {
        assertThatThrownBy(() -> themeService.deleteById(3L))
                .isInstanceOf(NoElementsException.class)
                .hasMessage("해당 테마가 존재하지 않습니다.");
    }

    @Test
    void 지난_일주일_간_인기_테마_10개를_조회한다() {
        List<Theme> themes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            themes.add(themeDbFixture.커스텀_테마("테마" + i));
        }
        Member member = memberDbFixture.유저1_생성();

        for (int i = 0; i < 20; i++) {
            addReservation(19 - i, member, ReservationDateFixture.예약날짜_오늘, reservationTimeDbFixture.열시(),
                    themes.get(i));
            addReservation(i, member, ReservationDateFixture.예약날짜_7일전, reservationTimeDbFixture.열시(), themes.get(i));
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

    private void addReservation(int count, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        for (int i = 0; i < count; i++) {
            reservationRepository.save(Reservation.reserve(
                    member, new ReservationDateTime(date, time), theme
            ));
        }
    }
}
