package roomescape.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    JpaMemberRepository memberRepository;
    @Autowired
    JpaReservationRepository reservationRepository;
    @Autowired
    JpaThemeRepository themeRepository;
    @Autowired
    JpaReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(null, "가이온", "hello@woowa.com", Role.USER, "password");
        memberRepository.save(member);

        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(10,0));
        reservationTimeRepository.save(reservationTime);

        Theme theme = new Theme(null, "테마1", "설명", "썸네일");
        themeRepository.save(theme);

        Reservation reservation1 = new Reservation(null, member, LocalDate.now().plusDays(1),reservationTime,theme);
        Reservation reservation2 = new Reservation(null, member, LocalDate.now().plusDays(2),reservationTime,theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);
    }

    @DisplayName("특정 기간동안의 멤버가 예약한 테마의 예약을 조회할 수 있다")
    @Test
    void findReservationByMemberAndThemeAndBetweenDate(){
        List<Reservation> byPeriod = reservationRepository.findByPeriod(
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                1L,
                1L);
        Assertions.assertThat(byPeriod.size()).isEqualTo(2);
    }
}
