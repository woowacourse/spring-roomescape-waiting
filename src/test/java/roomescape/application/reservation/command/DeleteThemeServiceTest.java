package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.ThemeException;

class DeleteThemeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private DeleteThemeService deleteThemeService;

    @BeforeEach
    void setUp() {
        deleteThemeService = new DeleteThemeService(themeRepository, reservationRepository);
    }

    @Test
    void 테마를_삭제할_수_있다() {
        // given
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));

        // when
        deleteThemeService.removeById(theme.getId());

        // then
        assertThat(themeRepository.findById(theme.getId())).isNotPresent();
    }

    @Test
    void 예약이_존재하는_테마는_삭제할_수_없다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), reservationTime, theme));

        // when
        // then
        assertThatThrownBy(() -> deleteThemeService.removeById(theme.getId()))
                .isInstanceOf(ThemeException.class)
                .hasMessage("해당 테마로 예약된 예약이 존재합니다.");
    }
}
