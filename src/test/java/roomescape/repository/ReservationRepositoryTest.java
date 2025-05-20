//package roomescape.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.DirtiesContext.ClassMode;
//import roomescape.domain.Reservation;
//import roomescape.domain.ReservationTheme;
//import roomescape.domain.ReservationTime;
//
//@JdbcTest
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
//@Disabled
//class ReservationRepositoryTest {
//
//    ReservationRepository repository;
//    @Autowired
//    JdbcTemplate template;
//
//    @BeforeEach
//    void setUp() {
//        repository = new ReservationRepositoryImpl(template, null);
//        template.execute("DELETE FROM reservation");
//        template.execute("DELETE FROM reservation_time");
//        template.execute("DELETE FROM reservation_theme");
//        template.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
//        template.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
//        template.execute("ALTER TABLE reservation_theme ALTER COLUMN id RESTART WITH 1");
//        template.execute("INSERT INTO reservation_theme (name, description, thumbnail)"
//                + "VALUES ('레벨 1탈출', '우테코 레벨1를 탈출하는 내용입니다.', 'https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg')");
//        template.execute("insert into reservation_time (start_at) values ('15:40')");
//        template.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
//                "브라운", LocalDate.now().plusDays(1).toString(), 1, 1);
//    }
//
//    @Test
//    void findById() {
//        //when
//        final LocalDate localDate = LocalDate.now().plusDays(1);
//        Reservation reservation = repository.findById(1L).get();
//
//        //then
//        assertEqualReservationElements(reservation, 1L, "브라운", localDate.toString(), 1L, "15:40");
//    }
//
//    @Test
//    void findAll() {
//        //when
//        final LocalDate localDate = LocalDate.now().plusDays(1);
//        List<Reservation> reservations = repository.findAll();
//
//        //then
//        assertThat(reservations).isNotEmpty();
//        assertEqualReservationElements(reservations.getFirst(), 1L, "브라운", localDate.toString(), 1L, "15:40");
//    }
//
//    @Test
//    void save() {
//        //given
//        final LocalDate localDate = LocalDate.now().plusDays(1);
//        Reservation reservation = new Reservation(
//                "네오",
//                localDate,
//                new ReservationTime(1L, LocalTime.of(15, 40)),
//                new ReservationTheme(1L, "테마", "테마", "테마")
//        );
//
//        //when
//        Reservation saved = repository.save(reservation);
//        Reservation firstReservation = repository.findById(1L).get();
//        Reservation secondReservation = repository.findById(2L).get();
//
//        //then
//        assertThat(repository.findAll()).hasSize(2);
//    }
//
//
//    @Test
//    void deleteById() {
//        //when
//        repository.deleteById(1L);
//
//        //then
//        assertThat(repository.findAll()).isEmpty();
//    }
//
//}
