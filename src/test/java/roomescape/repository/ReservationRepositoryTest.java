package roomescape.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Sql("/init.sql")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(1, 0)),
                new ReservationTime(LocalTime.of(2, 0))));

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
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).getId()).isEqualTo(1L));
    }

    @DisplayName("특정 멤버의 예약을 조회한다.")
    @Test
    void should_find_by_memberId() {
        List<Reservation> reservations = reservationRepository.findByMemberId(2L);
        assertAll(
                () -> assertThat(reservations).hasSize(1),
                () -> assertThat(reservations.get(0).getId()).isEqualTo(2L));
    }

    @DisplayName("특정 시간 아이디의 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_timeId() {
        boolean isExist = reservationRepository.existsByTimeId(1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 시간 아이디의 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_timeId() {
        boolean isExist = reservationRepository.existsByTimeId(3L);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 테마 아이디의 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_themeId() {
        boolean isExist = reservationRepository.existsByThemeId(1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 테마 아이디의 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_themeId() {
        boolean isExist = reservationRepository.existsByThemeId(3L);
        assertThat(isExist).isFalse();
    }
}
