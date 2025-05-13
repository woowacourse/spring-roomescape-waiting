package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;

@JdbcTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Disabled
class ReservationRepositoryTest {

    ReservationRepository repository;
    @Autowired
    JdbcTemplate template;

    @BeforeEach
    void setUp() {
        repository = new ReservationRepositoryImpl(template);
        template.execute("DELETE FROM reservation");
        template.execute("DELETE FROM reservation_time");
        template.execute("DELETE FROM reservation_theme");
        template.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        template.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        template.execute("ALTER TABLE reservation_theme ALTER COLUMN id RESTART WITH 1");
        template.execute("INSERT INTO reservation_theme (name, description, thumbnail)"
                + "VALUES ('레벨 1탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg')");
        template.execute("insert into reservation_time (start_at) values ('15:40')");
        template.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.now().plusDays(1).toString(), 1, 1);
    }

    @Test
    void findById() {
        //when
        final LocalDate localDate = LocalDate.now().plusDays(1);
        Reservation reservation = repository.findById(1L).get();

        //then
        assertEqualReservationElements(reservation, 1L, "브라운", localDate.toString(), 1L, "15:40");
    }

    @Test
    void findByDate() {
        //when
        final LocalDate localDate = LocalDate.now().plusDays(1);
        List<Reservation> found = repository.findByDate(LocalDate.of(localDate.getYear(), localDate.getMonth().getValue(), localDate.getDayOfMonth()));
        List<Reservation> notFound = repository.findByDate(LocalDate.of(2023, 8, 6));

        //then
        assertThat(found).hasSize(1);
        assertThat(notFound).isEmpty();
        assertEqualReservationElements(found.getFirst(), 1L, "브라운", localDate.toString(), 1L, "15:40");
    }

    @Test
    void findAll() {
        //when
        final LocalDate localDate = LocalDate.now().plusDays(1);
        List<Reservation> reservations = repository.findAll();

        //then
        assertThat(reservations).isNotEmpty();
        assertEqualReservationElements(reservations.getFirst(), 1L, "브라운", localDate.toString(), 1L, "15:40");
    }

    @Test
    void save() {
        //given
        final LocalDate localDate = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation("네오", localDate,
                ReservationTime.parse("15:40").toEntity(1L), new ReservationTheme(1L,"테마", "테마", "테마"));

        //when
        Reservation saved = repository.save(reservation);
        Reservation firstReservation = repository.findById(1L).get();
        Reservation secondReservation = repository.findById(2L).get();

        //then
        assertEqualReservationElements(saved, 2L, "네오",localDate.toString(), 1L, "15:40");
        assertEqualReservationElements(firstReservation, 1L, "브라운", localDate.toString(), 1L, "15:40");
        assertEqualReservationElements(secondReservation, 2L, "네오", localDate.toString(), 1L, "15:40");
        assertThat(repository.findAll()).hasSize(2);
    }


    @Test
    void deleteById() {
        //when
        int deleteCounts = repository.deleteById(1L);

        //then
        assertThat(deleteCounts).isEqualTo(1);
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void existsByDateAndTime() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDate anotherDate = LocalDate.parse("2023-08-06");
        ReservationTime time = ReservationTime.parse("15:40");
        ReservationTime anotherTime = ReservationTime.parse("15:41");

        //when
        boolean found = repository.existsByDateAndTime(date, time);
        boolean notFound1 = repository.existsByDateAndTime(date, anotherTime);
        boolean notFound2 = repository.existsByDateAndTime(anotherDate, time);
        boolean notFound3 = repository.existsByDateAndTime(anotherDate, anotherTime);

        //then
        assertThat(found).isTrue();
        assertThat(notFound1).isFalse();
        assertThat(notFound2).isFalse();
        assertThat(notFound3).isFalse();
    }

    private void assertEqualReservationElements(final Reservation reservation, final long id, final String name,
                                                final String date, final long timeId, final String time) {

        assertThat(reservation.getId()).isEqualTo(id);
        assertThat(reservation.getName()).isEqualTo(name);
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime().getId()).isEqualTo(timeId);
        assertThat(reservation.getTime().getStartAt()).isEqualTo(time);
    }

}
