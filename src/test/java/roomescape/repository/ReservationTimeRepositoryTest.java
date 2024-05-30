package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.service.fixture.TestMemberFactory.createMember;
import static roomescape.service.fixture.TestReservationFactory.createAcceptReservationAtNow;
import static roomescape.service.fixture.TestReservationTimeFactory.createReservationTime;
import static roomescape.service.fixture.TestThemeFactory.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

@DataJpaTest
@Sql("/init-data.sql")
class ReservationTimeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void should_get_reservation_times() {
        saveTime(createReservationTime(1L, "10:00"));
        saveTime(createReservationTime(2L, "11:00"));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes).hasSize(2);
    }

    @DisplayName("예약 시간을 추가한다")
    @Test
    void should_add_reservation_time() {
        saveTime(createReservationTime(1L, "10:00"));

        long count = reservationTimeRepository.count();

        assertThat(count).isEqualTo(1);
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_delete_reservation_time() {
        saveTime(createReservationTime(1L, "10:00"));
        saveTime(createReservationTime(2L, "11:00"));

        reservationTimeRepository.deleteById(1L);

        long count = reservationTimeRepository.count();

        assertThat(count).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약 시간을 조회한다.")
    @Test
    void should_get_reservation_time() {
        saveTime(createReservationTime(1L, "10:00"));

        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("아이디가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        saveTime(createReservationTime(1L, "10:00"));

        long count = reservationTimeRepository.countById(1L);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("시간에 해당하는 예약 시간의 개수를 조회한다.")
    @Test
    void should_return_reservation_time_count_when_give_start_at() {
        saveTime(createReservationTime(1L, "10:00"));
        saveTime(createReservationTime(2L, "11:00"));

        long count = reservationTimeRepository.countByStartAt(LocalTime.of(10, 0));
        assertThat(count).isEqualTo(1);
    }


    @DisplayName("주어진 날짜와 테마에 해당하는 예약된 시간을 조회한다.")
    @Test
    void should_return_reserved_time_when_give_date_and_theme() {
        ReservationTime reservedTime = saveTime(createReservationTime(1L, "10:00"));
        ReservationTime notReservedTime = saveTime(createReservationTime(2L, "11:00"));
        Member member = saveMember(createMember(1L));
        Theme theme = saveTheme(createTheme(1L));
        saveReservation(createAcceptReservationAtNow(1L, reservedTime, theme, member));

        List<ReservationTime> reservedTimes = reservationTimeRepository.findAllReservedTimes(LocalDate.now(), 1L);

        assertThat(reservedTimes).hasSize(1);
    }

    public ReservationTime saveTime(ReservationTime time) {
        entityManager.merge(time);
        return time;
    }

    public Member saveMember(Member member) {
        entityManager.merge(member);
        return member;
    }

    public Theme saveTheme(Theme theme) {
        entityManager.merge(theme);
        return theme;
    }

    public Reservation saveReservation(Reservation reservation) {
        entityManager.merge(reservation);
        return reservation;
    }
}
