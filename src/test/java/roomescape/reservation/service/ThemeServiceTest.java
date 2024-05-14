package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.Role;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.ThemeSaveRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ThemeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeService themeService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("중복된 테마 이름을 추가할 수 없다.")
    void duplicateThemeNameExceptionTest() {
        ThemeSaveRequest theme1 = new ThemeSaveRequest("공포", "무서운 테마", "https://ab.com/1x.png");
        themeService.save(theme1);

        ThemeSaveRequest theme2 = new ThemeSaveRequest("공포", "무서움", "https://cd.com/2x.jpg");
        assertThatThrownBy(() -> themeService.save(theme2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테마 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> themeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 해당 테마로 예약 되있을 경우 삭제 시 예외가 발생한다.")
    void deleteExceptionTest() {
        Long themeId = themeRepository.save(
                new Theme(new ThemeName("공포"), new Description("호러 방탈출"), "http://asdf.jpg"));
        Theme theme = themeRepository.findById(themeId).get();

        Long timeId = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId).get();

        Long memberId = memberRepository.save(new Member(1L, Role.MEMBER, new MemberName("카키"), "kaki@email.com", "1234"));
        Member member = memberRepository.findById(memberId).get();

        Reservation reservation = new Reservation(member, LocalDate.now(), theme, reservationTime);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> themeService.delete(themeId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
