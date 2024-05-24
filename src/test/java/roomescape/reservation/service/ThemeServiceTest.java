package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.CreateThemeRequest;
import roomescape.reservation.dto.response.CreateThemeResponse;
import roomescape.reservation.dto.response.FindPopularThemesResponse;
import roomescape.reservation.dto.response.FindThemeResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        themeRepository.deleteAll();
        reservationRepository.deleteAll();
    }

    @Test
    @DisplayName("방탈출 테마 생성 성공 시, 생성된 테마의 정보를 반환한다.")
    void createTheme() {
        CreateThemeRequest request = new CreateThemeRequest("테마이름", "설명", "썸네일");

        CreateThemeResponse response = themeService.createTheme(request);

        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.name()).isEqualTo("테마이름"),
                () -> assertThat(response.description()).isEqualTo("설명"),
                () -> assertThat(response.thumbnail()).isEqualTo("썸네일")
        );
    }

    @Test
    @DisplayName("방탈출 테마 목록을 조회한다.")
    void getThemes() {
        themeRepository.save(new Theme("테마이름1", "설명1", "썸네일1"));
        themeRepository.save(new Theme("테마이름2", "설명2", "썸네일2"));

        List<FindThemeResponse> response = themeService.getThemes();

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("name").containsExactlyInAnyOrder("테마이름1", "테마이름2")
        );
    }

    @Test
    @DisplayName("인기 방탈출 테마 목록을 조회한다.")
    void getPopularThemes() {
        themeRepository.save(new Theme("테마이름1", "설명1", "썸네일1"));
        themeRepository.save(new Theme("테마이름2", "설명2", "썸네일2"));
        Pageable pageable = PageRequest.of(0, 10);

        List<FindPopularThemesResponse> response = themeService.getPopularThemes(pageable);

        assertAll(
                () -> assertThat(response).hasSize(2),
                () -> assertThat(response).extracting("name").containsExactlyInAnyOrder("테마이름1", "테마이름2")
        );
    }

    @Test
    @DisplayName("방탈출 테마를 삭제한다.")
    void deleteTheme() {
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        themeService.deleteById(theme.getId());

        assertThat(themeRepository.findById(theme.getId())).isEmpty();
    }

    @Test
    @DisplayName("방탈출 테마 삭제 시, 삭제하려는 테마가 존재하지 않는 경우 예외를 반환한다.")
    void deleteTheme_WhenThemeNotExist() {
        assertThatThrownBy(() -> themeService.deleteById(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("식별자 999에 해당하는 테마가 존재하지 않습니다. 삭제가 불가능합니다.");
    }

    @Test
    @DisplayName("방탈출 테마 삭제 시, 사용 중인 테마인 경우 예외를 반환한다.")
    void deleteTheme_WhenThemeInUsage() {
        Member member = memberRepository.save(new Member("몰리", Role.USER, "login@naver.com", "hihi"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(20, 0)));
        Theme theme = themeRepository.save(new Theme("테마이름", "설명", "썸네일"));

        reservationRepository.save(new Reservation(member, LocalDate.now(), reservationTime, theme));

        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("테마를 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
    }
}
