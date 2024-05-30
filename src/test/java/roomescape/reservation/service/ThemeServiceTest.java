package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
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
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ThemeCreateRequest;
import roomescape.reservation.dto.response.PopularThemeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("중복된 테마 이름을 추가할 수 없다.")
    void duplicateThemeNameExceptionTest() {
        ThemeCreateRequest theme1 = new ThemeCreateRequest("공포", "무서운 테마", "https://ab.com/1x.png");
        themeService.save(theme1);

        ThemeCreateRequest theme2 = new ThemeCreateRequest("공포", "무서움", "https://cd.com/2x.jpg");
        assertThatThrownBy(() -> themeService.save(theme2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("인기테마를 조회한다.")
    void findPopularThemeTest() {
        Member member = memberRepository.save(new Member("hogi", "hoho@naver.com", "1234"));
        Theme theme1 = themeRepository.save(new Theme("a", "a", "a"));
        Theme theme2 = themeRepository.save(new Theme("b", "b", "b"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme2, time, Status.SUCCESS
                ));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme1, time, Status.SUCCESS
                ));
        Reservation reservation3 = reservationRepository.save(
                new Reservation(member, LocalDate.now(), theme2, time, Status.SUCCESS
                ));

        List<PopularThemeResponse> popularThemes = themeService.findPopularThemeBetweenWeekLimitTen();
        assertThat(popularThemes.get(0).name()).isEqualTo("b");
    }

    @Test
    @DisplayName("테마 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
