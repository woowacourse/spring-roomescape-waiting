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
import roomescape.infrastructure.error.exception.ReservationTimeException;

class DeleteReservationTimeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private DeleteReservationTimeService deleteReservationTimeService;

    @BeforeEach
    void setUp() {
        deleteReservationTimeService = new DeleteReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    void 예약시간을_삭제할_수_있다() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        // when
        deleteReservationTimeService.removeById(reservationTime.getId());

        // then
        assertThat(reservationTimeRepository.findById(1L))
                .isNotPresent();
    }

    @Test
    void 예약시간으로_예약된_예약이_존재하는_경우_예외가_발생한다() {
        // given
        Member member = memberRepository.save(new Member("test", new Email("test@test.com"), "test", MemberRole.ADMIN));
        Theme theme = themeRepository.save(new Theme("test", "test", "test"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), reservationTime, theme));

        // when
        // then
        assertThatThrownBy(() -> deleteReservationTimeService.removeById(reservationTime.getId()))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage("해당 예약 시간으로 예약된 예약이 존재합니다.");
    }
}
