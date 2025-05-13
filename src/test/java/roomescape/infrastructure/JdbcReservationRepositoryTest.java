package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.resetH2TableIds;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.infrastructure.jdbc.JdbcReservationRepository;

@JdbcTest
@Import(JdbcReservationRepository.class)
@ActiveProfiles("test")
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        resetH2TableIds(jdbcTemplate);
    }

    @DisplayName("save 후 생성된 id를 반환한다.")
    @Test
    void saveTest() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update(
                "INSERT INTO member (id, name, email, password, role) VALUES (1, '어드민', 'admin@email.com', 'password', 'ADMIN')");

        Reservation reservation = Reservation.withoutId(
                MEMBER1_ADMIN,
                Theme.of(1L, "테마1", "테마 1입니다.", "썸네일"),
                LocalDate.of(2025, 4, 30),
                ReservationTime.of(1L, LocalTime.of(10, 0))
        );

        // when
        Long savedId = reservationRepository.save(reservation);

        // then
        String sql = "SELECT * FROM reservation WHERE id = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, savedId);

        assertAll(
                () -> assertThat(savedId).isNotNull(),

                () -> assertThat(result).containsEntry("member_id", 1L),
                () -> assertThat(result).containsEntry("date", Date.valueOf(LocalDate.of(2025, 4, 30))),
                () -> assertThat(result).containsEntry("time_id", 1L)
        );
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void findAllTest() {
        // given
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00:00')");
        jdbcTemplate.update("""
                INSERT INTO member (name, email, password, role)
                VALUES ('어드민', 'admin@email.com', 'password', 'ADMIN'),
                       ('브라운', 'brown@email.com', 'brown', 'USER'),
                       ('브리', 'brie@email.com', 'brie', 'USER'),
                       ('솔라', 'solar@email.com', 'solar', 'USER')
                """);

        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (2, '2025-01-01', 1, 1)");
        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (3, '2025-01-01', 1, 1)");
        jdbcTemplate.update(
                "INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (4, '2025-01-01', 1, 1)");

        // when
        List<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).hasSize(3);
        assertThat(reservations)
                .extracting(reservation -> reservation.getMember().getName())
                .containsExactly("브라운", "브리", "솔라");
    }

    @DisplayName("id로 예약을 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        // given
        assertThat(reservationRepository.findAll()).isEmpty();
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail) VALUES (1, '테마1', '테마 1입니다.', '썸네일입니다.')");
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00:00')");
        jdbcTemplate.update(
                "INSERT INTO member (id, name, email, password, role) VALUES (1, '어드민', 'admin@email.com', 'password', 'ADMIN')");
        jdbcTemplate.update(
                "INSERT INTO reservation (id, member_id, date, time_id, theme_id) VALUES (1, 1, '2025-01-01', 1, 1)");
        assertThat(reservationRepository.findAll()).hasSize(1);

        // when
        reservationRepository.deleteById(1L);

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).isEmpty();
    }
}
