package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

@JdbcTest
public class ReservationDaoTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "테스트", "설명", "url");
    private final static Slot slot = Slot.restore(1L, LocalDate.parse("2027-05-27"), reservationTime, theme);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationRepository reservationDao;

    @BeforeEach
    void setUp() {
        this.reservationDao = new JdbcReservationRepository(jdbcTemplate);

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
        jdbcTemplate.update("insert into slot (date, time_id, theme_id) values ('2027-05-27', 1, 1)");
    }

    @Test
    void 예약을_생성하면_미결제_상태로_저장된다() {
        Long id = reservationDao.insert(Reservation.create("브라운", slot));

        Optional<Reservation> found = reservationDao.findReservationById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isPaid()).isFalse();
    }

    @Test
    void 결제_완료된_예약을_저장하면_결제_상태가_유지된다() {
        Long id = reservationDao.insert(Reservation.create("브라운", slot).updatePaid(true));

        Optional<Reservation> found = reservationDao.findReservationById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isPaid()).isTrue();
    }

    @Test
    void updatePaid는_결제_상태를_갱신한다() {
        Long id = reservationDao.insert(Reservation.create("브라운", slot));

        reservationDao.updatePaid(id, true);

        assertThat(reservationDao.findReservationById(id).orElseThrow().isPaid()).isTrue();
    }

    @Test
    void updateName은_결제_상태를_변경하지_않는다() {
        Long id = reservationDao.insert(Reservation.create("브라운", slot).updatePaid(true));

        reservationDao.updateName(id, "네오");

        Reservation found = reservationDao.findReservationById(id).orElseThrow();
        assertThat(found.getName()).isEqualTo("네오");
        assertThat(found.isPaid()).isTrue();
    }
}
