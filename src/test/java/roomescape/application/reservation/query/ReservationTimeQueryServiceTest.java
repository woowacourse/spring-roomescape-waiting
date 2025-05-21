package roomescape.application.reservation.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.query.dto.AvailableReservationTimeResult;
import roomescape.application.reservation.query.dto.ReservationTimeResult;
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

class ReservationTimeQueryServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private ReservationTimeQueryService reservationTimeQueryService;

    @BeforeEach
    void setUp() {
        reservationTimeQueryService = new ReservationTimeQueryService(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 모든_예약시간을_조회할_수_있다() {
        // given
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));

        // when
        List<ReservationTimeResult> results = reservationTimeQueryService.findAll();

        // then
        assertThat(results).containsExactlyInAnyOrder(
                new ReservationTimeResult(reservationTime1.getId(), LocalTime.of(12, 0)),
                new ReservationTimeResult(reservationTime2.getId(), LocalTime.of(13, 0))
        );
    }

    @Test
    void 예약시간을_예약여부와_함께_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("test", new Email("test@test.com"), "test", MemberRole.ADMIN));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), reservationTime1, theme));

        // when
        List<AvailableReservationTimeResult> availableTimesByThemeIdAndDate = reservationTimeQueryService.findAvailableTimesByThemeIdAndDate(
                theme.getId(),
                LocalDate.now().plusDays(1)
        );

        // then
        assertThat(availableTimesByThemeIdAndDate).containsExactlyInAnyOrder(
                new AvailableReservationTimeResult(reservationTime1.getId(), LocalTime.of(12, 0), true),
                new AvailableReservationTimeResult(reservationTime2.getId(), LocalTime.of(13, 0), false)
        );
    }
}
