//package roomescape.reservation.repository;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.jdbc.Sql;
//import roomescape.member.domain.Member;
//import roomescape.member.domain.Role;
//import roomescape.reservation.domain.Reservation;
//import roomescape.reservation.domain.ReservationTime;
//import roomescape.theme.domain.Theme;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataJpaTest
//class ReservationRepositoryTest {
//
//    @Autowired
//    private ReservationRepository reservationRepository;
//
//    @Test
//    void 예약_저장() {
//        // given
//        Member member = new Member("WooGa", "wooga@gmail.com", "1234", Role.USER);
//        member = memberRepository.save(member); // 여기서 id가 부여됨
//
//        Theme theme = new Theme("Theme1", "Description1","thumbnail1.jpg");
//        theme = themeRepository.save(theme);
//
//        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
//        reservationTime = reservationTimeRepository.save(reservationTime);
//
//        Reservation reservation = new Reservation(member, LocalDate.of(2025,12,25), reservationTime, theme);
//        reservationRepository.save(reservation);
//
//        // when
//        reservationRepository.save(reservation);
//
//        // then
//        assertThat(reservationRepository.findAll()).hasSize(1);
//    }
//
//}