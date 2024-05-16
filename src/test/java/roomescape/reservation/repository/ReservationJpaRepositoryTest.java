package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static roomescape.InitialDataFixture.INITIAL_RESERVATION_COUNT;
import static roomescape.InitialDataFixture.NOT_RESERVATED_TIME;
import static roomescape.InitialDataFixture.NOT_RESERVED_THEME;
import static roomescape.InitialDataFixture.NO_RESERVATION_DATE;
import static roomescape.InitialDataFixture.RESERVATION_1;
import static roomescape.InitialDataFixture.RESERVATION_2;
import static roomescape.InitialDataFixture.RESERVATION_3;
import static roomescape.InitialDataFixture.THEME3_MEMBER1_RESERVATION_COUNT;
import static roomescape.InitialDataFixture.THEME_2;
import static roomescape.InitialDataFixture.THEME_3;
import static roomescape.InitialMemberFixture.MEMBER_1;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ReservationJpaRepositoryTest {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Reservation을 저장하면 id가 포함된 Reservation이 반환된다.")
    void save() {
        Reservation reservation = new Reservation(
                RESERVATION_2.getDate(),
                RESERVATION_2.getReservationTime(),
                THEME_2,
                MEMBER_1
        );

        Reservation save = reservationJpaRepository.save(reservation);

        assertThat(save.getId()).isNotNull();
    }

    @Test
    @DisplayName("시간과 날짜만 같고 테마가 다른 경우 예약에 성공한다.")
    void saveOnlySameGetDateGetTime() {
        Reservation reservation = new Reservation(
                RESERVATION_2.getDate(),
                RESERVATION_2.getReservationTime(),
                THEME_2,
                MEMBER_1
        );
        assertThatNoException().isThrownBy(() -> reservationJpaRepository.save(reservation));
    }

    @Test
    @DisplayName("테마가 같고 날짜가 다른 경우 예약에 성공한다.")
    void saveOnlySameTheme() {
        Reservation reservation = new Reservation(
                RESERVATION_2.getDate().plusDays(1),
                RESERVATION_2.getReservationTime(),
                RESERVATION_2.getTheme(),
                MEMBER_1
        );
        assertThatNoException().isThrownBy(() -> reservationJpaRepository.save(reservation));
    }

    @Test
    @DisplayName("이미 존재하는 예약과 날짜, 시간, 테마가 같으면 중복되는 예약이다.")
    void isAlreadyBooked() {
        boolean alreadyBooked = reservationJpaRepository.existsByDateAndReservationTimeAndTheme(
                RESERVATION_1.getDate(),
                RESERVATION_1.getReservationTime(),
                RESERVATION_1.getTheme()
        );

        assertThat(alreadyBooked).isEqualTo(true);
    }

    @Test
    @DisplayName("이미 존재하는 예약과 날짜가 다르면 중복되는 예약이 아니다.")
    void isNotBookedDate() {
        boolean alreadyBooked = reservationJpaRepository.existsByDateAndReservationTimeAndTheme(
                NO_RESERVATION_DATE,
                RESERVATION_1.getReservationTime(),
                RESERVATION_1.getTheme()
        );

        assertThat(alreadyBooked).isEqualTo(false);
    }

    @Test
    @DisplayName("이미 존재하는 예약과 시간이 다르면 중복되는 예약이 아니다.")
    void isNotBookedTime() {
        boolean alreadyBooked = reservationJpaRepository.existsByDateAndReservationTimeAndTheme(
                RESERVATION_1.getDate(),
                NOT_RESERVATED_TIME,
                RESERVATION_1.getTheme()
        );

        assertThat(alreadyBooked).isEqualTo(false);
    }

    @Test
    @DisplayName("이미 존재하는 예약과 테마가 다르면 중복되는 예약이 아니다.")
    void isNotBookedTheme() {
        boolean alreadyBooked = reservationJpaRepository.existsByDateAndReservationTimeAndTheme(
                RESERVATION_1.getDate(),
                RESERVATION_1.getReservationTime(),
                NOT_RESERVED_THEME
        );

        assertThat(alreadyBooked).isEqualTo(false);
    }

    @Test
    @DisplayName("저장된 모든 Reservation을 반환한다.")
    void findAll() {
        Iterable<Reservation> found = reservationJpaRepository.findAll();

        assertThat(found).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @Test
    @DisplayName("저장된 모든 Reservation 중 themeId, memberId가 일치하는 Reservation들만 반환한다.")
    void findAllWithThemeIdAndMemberId() {
        List<Reservation> found = reservationJpaRepository.findByThemeAndMember(THEME_3, MEMBER_1);

        assertThat(found).hasSize(THEME3_MEMBER1_RESERVATION_COUNT);
    }

    @Test
    @DisplayName("특정 날짜와 테마에 예약된 예약들을 반환한다.")
    void findByDateAndTheme() {
        List<Reservation> reservations = reservationJpaRepository.findByDateAndTheme(
                RESERVATION_1.getDate(),
                RESERVATION_1.getTheme()
        );

        assertThat(reservations).containsExactlyInAnyOrder(RESERVATION_1);
    }

    @Test
    @DisplayName("특정 회원이 예약한 예약들을 반환한다.")
    void findByMember() {
        List<Reservation> reservations = reservationJpaRepository.findByMember(MEMBER_1);

        assertThat(reservations).containsExactlyInAnyOrder(RESERVATION_1, RESERVATION_2, RESERVATION_3);
    }

    @Test
    @DisplayName("Reservation을 제거한다.")
    void delete() {
        reservationJpaRepository.deleteById(RESERVATION_1.getId());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);

        assertThat(count).isEqualTo(INITIAL_RESERVATION_COUNT - 1);
    }
}
