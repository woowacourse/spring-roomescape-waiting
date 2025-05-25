package roomescape.theme;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.booking.reservation.Reservation;
import roomescape.booking.reservation.ReservationRepository;
import roomescape.booking.schedule.Schedule;
import roomescape.booking.schedule.ScheduleRepository;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.member.MemberRole;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("기간 내 인기 테마를 순서대로 조회할 수 있다")
    void findPopularThemes() {
        // given
        Theme savedTheme1 = themeRepository.save(new Theme("top1", "description", "abc"));
        Theme savedTheme2 = themeRepository.save(new Theme("top2", "description", "abc"));
        Theme savedTheme3 = themeRepository.save(new Theme("top3", "description", "abc"));

        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        Schedule schedule1OfTheme1 = scheduleRepository.save(new Schedule(LocalDate.now(), savedReservationTime, savedTheme1));
        Schedule schedule2OfTheme1 = scheduleRepository.save(new Schedule(LocalDate.now().plusDays(1), savedReservationTime, savedTheme1));
        Schedule schedule3OfTheme1 = scheduleRepository.save(new Schedule(LocalDate.now().plusDays(2), savedReservationTime, savedTheme1));
        Schedule schedule1OfTheme2 = scheduleRepository.save(new Schedule(LocalDate.now(), savedReservationTime, savedTheme2));
        Schedule schedule2OfTheme2 = scheduleRepository.save(new Schedule(LocalDate.now().plusDays(1), savedReservationTime, savedTheme2));
        Schedule schedule1OfTheme3 = scheduleRepository.save(new Schedule(LocalDate.now(), savedReservationTime, savedTheme3));

        Member member = new Member("may@example.com", "1234", "메이", MemberRole.MEMBER);
        Member savedMember = memberRepository.save(member);

        reservationRepository.save(new Reservation(savedMember, schedule1OfTheme1));
        reservationRepository.save(new Reservation(savedMember, schedule2OfTheme1));
        reservationRepository.save(new Reservation(savedMember, schedule3OfTheme1));
        reservationRepository.save(new Reservation(savedMember, schedule1OfTheme2));
        reservationRepository.save(new Reservation(savedMember, schedule2OfTheme2));
        reservationRepository.save(new Reservation(savedMember, schedule1OfTheme3));

        // when
        List<Theme> themes = themeRepository.findAllOrderByRank(LocalDate.now(), LocalDate.now().plusDays(2), 3);

        // then
        Assertions.assertThat(themes).containsExactly(savedTheme1, savedTheme2, savedTheme3);
    }
}
