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
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.dto.request.ThemeRequest;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.BadRequestException;

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
    @Sql("/popular-themes.sql")
    @DisplayName("인기 테마들을 조회한다.")
    void getPopularThemes() {
        LocalDate stateDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 10);
        int limit = 3;

        List<ThemeResponse> themeResponses = themeService.getPopularThemes(stateDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeResponses).hasSize(3);

            softly.assertThat(themeResponses.get(0).id()).isEqualTo(4);
            softly.assertThat(themeResponses.get(0).name()).isEqualTo("마법의 숲");
            softly.assertThat(themeResponses.get(0).description()).isEqualTo("요정과 마법사들이 사는 신비로운 숲 속으로!");
            softly.assertThat(themeResponses.get(0).thumbnail()).isEqualTo("https://via.placeholder.com/150/30f9e7");

            softly.assertThat(themeResponses.get(1).id()).isEqualTo(3);
            softly.assertThat(themeResponses.get(1).name()).isEqualTo("시간여행");
            softly.assertThat(themeResponses.get(1).description()).isEqualTo("과거와 미래를 오가며 역사의 비밀을 밝혀보세요.");
            softly.assertThat(themeResponses.get(1).thumbnail()).isEqualTo("https://via.placeholder.com/150/24f355");

            softly.assertThat(themeResponses.get(2).id()).isEqualTo(2);
            softly.assertThat(themeResponses.get(2).name()).isEqualTo("우주 탐험");
            softly.assertThat(themeResponses.get(2).description()).isEqualTo("끝없는 우주에 숨겨진 비밀을 파헤치세요.");
            softly.assertThat(themeResponses.get(2).thumbnail()).isEqualTo("https://via.placeholder.com/150/771796");
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

        reservationRepository.save(Reservation.create(
                LocalDateTime.of(2024, 4, 6, 10, 30),
                LocalDate.of(2024, 4, 7),
                member,
                reservationTime,
                theme,
                ReservationStatus.RESERVED
        ));

        Long themeId = theme.getId();

        assertThatThrownBy(() -> themeService.deleteThemeById(themeId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 테마를 사용하는 예약이 존재합니다.");

    }
}
