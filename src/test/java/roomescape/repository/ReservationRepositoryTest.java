package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.member.Role;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql("/truncate.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        themeRepository.saveAll(List.of(
                new Theme("n1", "d1", "t1"),
                new Theme("n2", "d2", "t2")));

        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(1, 0)),
                new ReservationTime(LocalTime.of(2, 0))));

        memberRepository.saveAll(List.of(
                new Member("에버", "treeboss@gmail.com", "treeboss123!", Role.USER),
                new Member("우테코", "wtc@gmail.com", "wtc123!", Role.ADMIN)));

        reservationRepository.saveAll(List.of(
                new Reservation(
                        LocalDate.of(2000,1,1),
                        new ReservationTime(1, null),
                        new Theme(1, null, null, null),
                        new Member(1, null, null, null, null)),
                new Reservation(LocalDate. of(2000, 1, 2),
                        new ReservationTime(2, null),
                        new Theme(2, null, null, null),
                        new Member(2, null, null, null, null))));
    }

    @DisplayName("특정 날짜와 시간과 테마를 가진 예약이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_reservation_by_date_and_timeId() {
        boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.of(2000, 1, 1), 1L, 1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 날짜와 시간과 테마를 가진 예약이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_reservation_by_date_and_timeId() {
        boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.of(9999, 1, 1), 1L, 1L);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 날짜와 테마의 예약된 시간을 조회한다.")
    @Test
    void should_find_booked_reservation_time() {
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeBooked(LocalDate.of(2000, 1, 1), 1L);
        assertThat(bookedTimes).containsExactly(new ReservationTime(1L, LocalTime.of(1, 0)));
    }

    @DisplayName("특정 멤버와 날짜와 테마의 예약을 조회한다.")
    @Test
    void should_find_reservation_by_memberId_and_themeId_and_date() {
        List<Reservation> reservations = reservationRepository.findByMemberIdAndThemeIdAndDate(1L, 1L, LocalDate.of(999, 1, 1), LocalDate.of(3000, 1, 1));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getId()).isEqualTo(1L);
    }
}