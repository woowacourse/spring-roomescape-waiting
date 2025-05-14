package roomescape.application.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.dto.ReservationWithStatusResult;
import roomescape.domain.member.Email;
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

class PersonalReservationServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private PersonalReservationService personalReservationService;

    @BeforeEach
    void setUp() {
        personalReservationService = new PersonalReservationService(reservationRepository);
    }

    @Test
    void 검색_조건에_맞는_예약을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", Role.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock), time1, theme));
        reservationRepository.save(new Reservation(member, LocalDate.now(clock).plusDays(1), time2, theme));

        // when
        List<ReservationWithStatusResult> reservationsWithStatus = personalReservationService.findReservationsWithStatus(
                member.getId()
        );

        // then
        assertThat(reservationsWithStatus)
                .isEqualTo(List.of(
                        new ReservationWithStatusResult(
                                1L,
                                "테마",
                                LocalDate.now(clock),
                                LocalTime.of(13, 0),
                                ReservationStatus.RESERVE
                        ),
                        new ReservationWithStatusResult(
                                2L,
                                "테마",
                                LocalDate.now(clock).plusDays(1),
                                LocalTime.of(14, 0),
                                ReservationStatus.RESERVE
                        )
                ));
    }
}
