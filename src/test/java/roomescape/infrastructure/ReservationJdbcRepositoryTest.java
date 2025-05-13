package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.DomainFixtures;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;

@Sql(scripts = {"/test-schema.sql"})
@JdbcTest
class ReservationJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationRepository repository;

    @DisplayName("타임 슬롯과 테마를 미리 세팅")
    @BeforeEach
    void setUp() {
        jdbcTemplate.update("insert into USERS (id, name, role, email, password) values (?, ?, ?, ?, ?)", 1, "popo", "USER", "popo@email.com", "password");
        jdbcTemplate.update("insert into RESERVATION_TIME (id, start_at) values (?, ?)", 1, "10:00");
        jdbcTemplate.update("insert into THEME (id, name, description, thumbnail) values (?, ?, ?, ?)",
            1,
            "레벨2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        );

        repository = new ReservationJdbcRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약을 아이디로 조회한다.")
    void findById() {
        // given
        var reservation = readyReservation();
        var savedId = repository.save(reservation);

        // when
        Optional<Reservation> found = repository.findById(savedId);

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("예약을 저장한다.")
    void save() {
        // given
        var reservation = readyReservation();

        // when
        repository.save(reservation);

        // then
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void removeById() {
        // given
        var reservation = readyReservation();
        var savedId = repository.save(reservation);

        // when
        boolean isRemoved = repository.removeById(savedId);

        // then
        assertAll(
            () -> assertThat(isRemoved).isTrue(),
            () -> assertThat(repository.findAll()).isEmpty()
        );
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void findAll() {
        // given
        var reservation1 = readyReservation();
        var reservation2 = readyReservation();
        repository.save(reservation1);
        repository.save(reservation2);

        // when
        var reservations = repository.findAll();

        // then
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("타임 슬롯 아이디에 해당하는 예약을 조회한다.")
    void findByTimeSlotId() {
        // given
        var reservation = readyReservation();
        repository.save(reservation);

        // when
        var found = repository.findByTimeSlotId(reservation.timeSlot().id());

        // then
        assertThat(found).contains(reservation);
    }

    @Test
    @DisplayName("테마 아이디에 해당하는 예약을 조회한다.")
    void findByThemeId() {
        // given
        var reservation = readyReservation();
        repository.save(reservation);

        // when
        var found = repository.findByThemeId(reservation.theme().id());

        // then
        assertThat(found).contains(reservation);
    }

    @Test
    @DisplayName("날짜와 테마 아이디에 해당하는 예약을 조회한다.")
    void findByDateAndThemeId() {
        // given
        var reservation = readyReservation();
        repository.save(reservation);

        // when
        var found = repository.findByDateAndThemeId(reservation.date(), reservation.theme().id());

        // then
        assertThat(found).contains(reservation);
    }

    @Test
    @DisplayName("날짜와 타임 슬롯, 테마 아이디에 해당하는 예약을 조회한다.")
    void findByDateAndTimeSlotAndThemeId() {
        // given
        var reservation = readyReservation();
        repository.save(reservation);

        // when
        var date = reservation.date();
        var timeSlotId = reservation.timeSlot().id();
        var themeId = reservation.theme().id();
        var found = repository.findByDateAndTimeSlotAndThemeId(date, timeSlotId, themeId);

        // then
        assertThat(found).contains(reservation);
    }

    private Reservation readyReservation() {
        return Reservation.ofExisting(
            1L,
            DomainFixtures.JUNK_USER,
            LocalDate.of(2023, 12, 1),
            DomainFixtures.JUNK_TIME_SLOT,
            DomainFixtures.JUNK_THEME
        );
    }
}
