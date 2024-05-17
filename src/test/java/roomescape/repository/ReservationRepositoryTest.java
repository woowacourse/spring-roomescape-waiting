package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository timeRepository;

    @Test
    @DisplayName("멤버 이메일을 통해 모든 예약을 조회한다")
    void findAllByMemberEmail() {
        // given
        Member member = memberRepository.save(new Member("seyang@test.com", "seyang", "Seyang"));
        Theme theme = themeRepository.save(new Theme("Theme 1", "Desc 1", "Thumb 1"));
        ReservationTime time = timeRepository.save(new ReservationTime("10:00"));
        LocalDate date = LocalDate.now().plusDays(1);

        Reservation reservation = reservationRepository.save(new Reservation(member, theme, date, time));

        // when
        List<Reservation> actual = reservationRepository.findAllByMemberEmail(member.getEmail());

        // then
        assertThat(actual).contains(reservation);
    }
}