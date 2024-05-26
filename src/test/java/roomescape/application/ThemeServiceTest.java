package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.ThemeRequest;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;
import roomescape.fixture.Fixture;

class ThemeServiceTest extends BaseServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("테마를 추가한다.")
    void addTheme() {
        ThemeRequest request = new ThemeRequest("테마", "테마 설명", "https://example.com");

        ThemeResponse themeResponse = themeService.addTheme(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponse.id()).isNotNull();
            softly.assertThat(themeResponse.name()).isEqualTo("테마");
            softly.assertThat(themeResponse.description()).isEqualTo("테마 설명");
            softly.assertThat(themeResponse.thumbnail()).isEqualTo("https://example.com");
        });
    }

    @Test
    @DisplayName("테마를 추가할 때, 이미 존재하는 이름이 있으면 예외를 발생시킨다.")
    void addThemeFailWhenNameAlreadyExists() {
        String name = "테마";

        themeRepository.save(new Theme(name, "테마 설명", "https://example.com"));

        ThemeRequest request = new ThemeRequest(name, "테마 설명1", "https://example1.com");

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> themeService.addTheme(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(String.format("해당 이름의 테마는 이미 존재합니다. (이름: %s)", name));
        });
    }

    @Test
    @DisplayName("모든 테마들을 조회한다.")
    void getAllThemes() {
        themeRepository.save(new Theme("테마1", "테마 설명", "https://example.com"));

        List<ThemeResponse> themeResponses = themeService.getAllThemes();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponses).hasSize(1);
            softly.assertThat(themeResponses.get(0).name()).isEqualTo("테마1");
            softly.assertThat(themeResponses.get(0).description()).isEqualTo("테마 설명");
            softly.assertThat(themeResponses.get(0).thumbnail()).isEqualTo("https://example.com");
        });
    }

    @Test
    @DisplayName("특정 기간 중 예약이 많은 순으로 인기 테마를 조회한다.")
    void getPopularThemes() {
        LocalDate stateDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 7);
        int limit = 2;

        LocalDate includedDate = LocalDate.of(2024, 4, 6);
        LocalDate excludedDate = LocalDate.of(2024, 4, 8);

        Member member = memberRepository.save(Fixture.MEMBER_1);

        Theme theme1 = themeRepository.save(Fixture.THEME_1);
        Theme theme2 = themeRepository.save(Fixture.THEME_2);

        ReservationTime time1 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_1);
        ReservationTime time2 = reservationTimeRepository.save(Fixture.RESERVATION_TIME_2);

        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time1, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme2), member));
        reservationRepository.save(new Reservation(new ReservationDetail(includedDate, time2, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time1, theme1), member));
        reservationRepository.save(new Reservation(new ReservationDetail(excludedDate, time2, theme1), member));

        List<ThemeResponse> themeResponses = themeService.getPopularThemes(stateDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponses).hasSize(2);

            softly.assertThat(themeResponses.get(0).id()).isEqualTo(theme2.getId());
            softly.assertThat(themeResponses.get(0).name()).isEqualTo(theme2.getName());

            softly.assertThat(themeResponses.get(1).id()).isEqualTo(theme1.getId());
            softly.assertThat(themeResponses.get(1).name()).isEqualTo(theme1.getName());
        });
    }

    @Test
    @DisplayName("id로 테마를 삭제한다.")
    void deleteThemeById() {
        Theme theme = themeRepository.save(new Theme("테마1", "테마 설명", "https://example.com"));

        themeService.deleteThemeById(theme.getId());

        assertThat(themeRepository.findById(theme.getId())).isEmpty();
    }

    @Test
    @DisplayName("id로 테마를 삭제할 때, 해당 id의 테마가 존재하지 않으면 예외를 발생시킨다.")
    void deleteThemeByIdFailWhenThemeNotFound() {
        assertThatThrownBy(() -> themeService.deleteThemeById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 테마가 존재하지 않습니다. (id: %d)", -1L));
    }

    @Test
    @DisplayName("id로 테마를 삭제할 때, 해당 테마를 사용하는 예약이 존재하면 예외를 발생시킨다.")
    void deleteThemeByIdFailWhenReservationExists() {
        Theme theme = themeRepository.save(new Theme("테마1", "테마 설명", "https://example.com"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        Member member = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));

        LocalDateTime now = LocalDateTime.of(2024, 4, 6, 10, 30);
        LocalDate reservationDate = LocalDate.of(2024, 4, 8);
        ReservationDetail detail = new ReservationDetail(reservationDate, reservationTime, theme);

        reservationRepository.save(Reservation.create(now, detail, member));

        Long themeId = theme.getId();

        assertThatThrownBy(() -> themeService.deleteThemeById(themeId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 테마를 사용하는 예약이 존재합니다.");
    }
}
