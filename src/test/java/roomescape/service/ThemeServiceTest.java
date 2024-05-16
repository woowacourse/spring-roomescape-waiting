package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.reservation.ThemeUsingException;
import roomescape.repository.DatabaseCleanupListener;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@TestExecutionListeners({
        DatabaseCleanupListener.class,
        DependencyInjectionTestExecutionListener.class
})
@Import(ThemeService.class)
@DataJpaTest
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("테마 삭제 시 해당 테마에 예약이 존재하면 에러를 발생시킨다.")
    @Test
    void throw_exception_when_delete_theme_with_existing_reservation() {
        Member member = new Member("t1@t1.com", "123", "러너덕", "MEMBER");
        Theme theme = new Theme("공포", "공포는 무서워", "hi.jpg");
        LocalDate date = LocalDate.parse("2025-11-30");
        ReservationTime time = new ReservationTime("11:00");
        Reservation reservation = new Reservation(member, theme, date, time);

        memberRepository.save(member);
        reservationTimeRepository.save(time);
        themeRepository.save(theme);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(ThemeUsingException.class)
                .hasMessage("해당 테마에 예약이 있어 삭제할 수 없습니다.");
    }

    @DisplayName("테마 삭제에 성공한다.")
    @Test
    void success_delete_theme() {
        Theme theme = new Theme("공포", "공포는 무서워", "hi.jpg");
        themeRepository.save(theme);

        assertThatNoException()
                .isThrownBy(() -> themeService.deleteTheme(1L));
    }
}
