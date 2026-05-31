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
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
class JdbcReservationRepositoryTest {

    private static final long DATE_ID = 101L;
    private static final long TIME_ID = 201L;
    private static final long THEME_ID = 301L;
    private static final LocalDate PLAY_DAY = LocalDate.of(2026, 5, 15);
    private static final LocalTime START_AT = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationRepository reservationRepository;

    private ReservationDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        reservationRepository = new JdbcReservationRepository(jdbcTemplate);

        jdbcTemplate.update("insert into reservation_date(id, play_day) values (?, ?)", DATE_ID, PLAY_DAY.toString());
        reservationDate = ReservationDate.of(DATE_ID, PLAY_DAY);

        jdbcTemplate.update("insert into reservation_time(id, start_at) values (?, ?)", TIME_ID, START_AT.toString());
        reservationTime = ReservationTime.of(TIME_ID, START_AT);

        jdbcTemplate.update("insert into theme(id, name, content, url) values (?, ?, ?, ?)",
                THEME_ID, "테마", "설명", "url");
        theme = Theme.of(THEME_ID, "테마", "설명", "url");
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
