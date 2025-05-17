package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DEFAULT_DATE;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.service.result.ReservationResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 전체_예약을_조회할_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation1 = reservationRepository.save(TestFixture.createDefaultReservation(member, DEFAULT_DATE, reservationTime, theme));
        Reservation reservation2 = reservationRepository.save(TestFixture.createDefaultReservation(member, DEFAULT_DATE.plusDays(1), reservationTime, theme));


        //when
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(null, null, null, null);

        //then
        assertAll(
                () -> assertThat(reservationResults).hasSize(2),
                () -> assertThat(reservationResults.getFirst())
                        .isEqualTo(ReservationResult.from(reservation1))
        );
    }

    @Test
    void id값으로_예약을_삭제할_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = reservationRepository.save(TestFixture.createDefaultReservation(member, DEFAULT_DATE, reservationTime, theme));

        //when
        reservationService.deleteById(reservation.getId());

        //then
        assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
    }

}
