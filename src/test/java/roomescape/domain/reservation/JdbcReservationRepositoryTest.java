package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
@Sql("/truncate.sql")
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationRepository reservationRepository;

    private ReservationDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);

        jdbcTemplate.update("insert into reservation_date(play_day) values (?)", "2026-05-15");
        reservationDate = ReservationDate.of(1L, LocalDate.parse("2026-05-15"));

        jdbcTemplate.update("insert into reservation_time(start_at) values (?)", "10:00");
        reservationTime = ReservationTime.of(1L, LocalTime.parse("10:00"));

        jdbcTemplate.update("insert into theme(name, content, url) values (?, ?, ?)", "테마", "설명", "url");
        theme = Theme.of(1L, "테마", "설명", "url");
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        Reservation reservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        Reservation saved = reservationRepository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void findAll() {
        int beforeSize = reservationRepository.findAll().size();
        Reservation reservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(beforeSize + 1);
    }

    @Test
    @DisplayName("ID로 예약을 삭제한다.")
    void deleteById() {
        Reservation reservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        Reservation saved = reservationRepository.save(reservation);
        int beforeSize = reservationRepository.findAll().size();

        int deletedCount = reservationRepository.deleteById(saved.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(reservationRepository.findAll()).hasSize(beforeSize - 1);
        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("이름으로 예약을 조회한다.")
    void findByName() {
        Reservation reservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        reservationRepository.save(reservation);

        List<Reservation> reservations = reservationRepository.findByName("테스터");

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getName()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("날짜, 시간, 테마가 중복되는 예약이 있는지 확인한다.")
    void existsByDateIdAndTimeIdAndThemeId() {
        Reservation reservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        reservationRepository.save(reservation);

        boolean exists = reservationRepository.existsByDateIdAndTimeIdAndThemeId(
            reservationDate.getId(), reservationTime.getId(), theme.getId());

        assertThat(exists).isTrue();
    }
}
