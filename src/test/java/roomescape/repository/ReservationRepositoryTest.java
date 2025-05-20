package roomescape.repository;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("")
    void test1() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        Member member = memberRepository.findById(1L).get();

        Reservation reservation = new Reservation(LocalDate.now().plusDays(1), reservationTime, theme, member,
                LocalDate.now());
        Reservation savedRe = reservationRepository.save(reservation);

        System.out.println("======");

        reservationRepository.findByReservationTimeId(savedRe.getId());

        // when

        // then
    }
}
