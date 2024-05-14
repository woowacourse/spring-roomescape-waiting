package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.JdbcMemberRepository;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JdbcReservationTimeRepository;
import roomescape.repository.JdbcThemeRepository;

@TestExecutionListeners(value = {
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThemeServiceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Autowired
    private ThemeService themeService;

    @Autowired
    private JdbcReservationTimeRepository reservationTimeRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcMemberRepository memberRepository;

    @DisplayName("테마 삭제 시 저장되어있지 않은 아이디면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_delete_not_saved_theme_id() {
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 아이디입니다.");
    }

    @DisplayName("테마 삭제 시 해당 테마에 예약이 존재하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_delete_theme_with_existing_reservation() {
        Member member = new Member(1L, "t1@t1.com", "123", "러너덕", "MEMBER");
        Theme theme = new Theme(1L, "공포", "공포는 무서워", "hi.jpg");
        ReservationDate date = new ReservationDate("2025-11-30");
        ReservationTime time = new ReservationTime(1L, "11:00");
        Reservation reservation = new Reservation(1L, member, theme, date, time);

        memberRepository.insertMember(member);
        reservationTimeRepository.insertReservationTime(time);
        themeRepository.insertTheme(theme);
        reservationRepository.insertReservation(reservation);

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 테마에 예약이 있어 삭제할 수 없습니다.");
    }

    @DisplayName("테마 삭제에 성공한다.")
    @Test
    void success_delete_theme() {
        Theme theme = new Theme(1L, "공포", "공포는 무서워", "hi.jpg");
        themeRepository.insertTheme(theme);

        assertThatNoException()
                .isThrownBy(() -> themeService.deleteTheme(1L));
    }
}
