package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.Fixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@SpringBootTest
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(value = "/clear.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class JdbcReservationRepositoryTest {
    private static final Member DEFAULT_MEMBER = Fixture.defaultMember;
    private static final ReservationTime DEFAULT_TIME = new ReservationTime(1L, LocalTime.of(11, 56));
    private static final Theme DEFAULT_THEME = new Theme(1L, "이름", "설명", "썸네일");

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.update("insert into MEMBER(name, role, email, password) VALUES (?, ?, ?, ?)",
                DEFAULT_MEMBER.getName(), DEFAULT_MEMBER.getRole().toString(),
                DEFAULT_MEMBER.getEmail(),
                DEFAULT_MEMBER.getPassword());
        jdbcTemplate.update("insert into reservation_time(start_at) values('11:56')");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("ALTER TABLE theme alter column id restart with 1");
        jdbcTemplate.update(
                "insert into theme (name, description, thumbnail) values('name', 'description', 'thumbnail')");

    }

    @Test
    @DisplayName("Reservation 을 잘 저장하는지 확인한다.")
    void save() {
        var beforeSave = reservationRepository.findAll().getReservations();
        Reservation saved = reservationRepository.save(
                new Reservation(LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME, DEFAULT_MEMBER.getLoginMember()));
        var afterSave = reservationRepository.findAll().getReservations();

        Assertions.assertThat(afterSave)
                .containsAll(beforeSave)
                .contains(saved);
    }

    @Test
    @DisplayName("Reservation 을 잘 조회하는지 확인한다.")
    void findAll() {
        List<Reservation> beforeSave = reservationRepository.findAll().getReservations();
        reservationRepository.save(
                new Reservation(LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME, DEFAULT_MEMBER.getLoginMember()));
        reservationRepository.save(
                new Reservation(LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME, DEFAULT_MEMBER.getLoginMember()));

        List<Reservation> afterSave = reservationRepository.findAll().getReservations();
        Assertions.assertThat(afterSave.size())
                .isEqualTo(beforeSave.size() + 2);
    }

    @Test
    @DisplayName("Reservation 을 잘 지우는지 확인한다.")
    void delete() {
        List<Reservation> beforeSaveAndDelete = reservationRepository.findAll().getReservations();
        reservationRepository.save(
                new Reservation(LocalDate.now(), DEFAULT_TIME, DEFAULT_THEME, DEFAULT_MEMBER.getLoginMember()));

        reservationRepository.delete(1L);

        List<Reservation> afterSaveAndDelete = reservationRepository.findAll().getReservations();

        Assertions.assertThat(beforeSaveAndDelete)
                .containsExactlyElementsOf(afterSaveAndDelete);
    }
}
