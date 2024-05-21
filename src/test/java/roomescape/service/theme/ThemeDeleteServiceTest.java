package roomescape.service.theme;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;

class ThemeDeleteServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeDeleteService themeDeleteService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
    }

    @Test
    @DisplayName("예약 중이 아닌 테마를 삭제할 시 성공한다.")
    void deleteNotReservedTime_Success() {
        assertThatCode(() -> themeDeleteService.deleteTheme(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약 중인 테마를 삭제할 시 예외가 발생한다.")
    void deleteReservedTime_Failure() {
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1L), time, theme));

        assertThatThrownBy(() -> themeDeleteService.deleteTheme(1L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 예약중인 테마는 삭제할 수 없습니다.");
    }
}
