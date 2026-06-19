package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.slot.SlotRepository;
import roomescape.domain.theme.Theme;

@JdbcTest
public class SlotDaoTest {

    private final static LocalDate date = LocalDate.parse("2027-05-27");
    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "테스트", "설명", "url");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SlotRepository slotDao;

    @BeforeEach
    void setUp() {
        this.slotDao = new JdbcSlotRepository(jdbcTemplate);

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");
        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
        jdbcTemplate.update("insert into slot (date, time_id, theme_id) values ('2027-05-27', 1, 1)");
    }

    @Test
    void 날짜_시간_테마로_슬롯을_조회한다() {
        Optional<Slot> found = slotDao.findByDateAndTimeAndTheme(date, 1L, 1L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(1L);
        assertThat(found.get().getDate()).isEqualTo(date);
        assertThat(found.get().getTime().getId()).isEqualTo(1L);
        assertThat(found.get().getTheme().getId()).isEqualTo(1L);
    }

    @Test
    void 일치하는_슬롯이_없으면_빈_Optional을_반환한다() {
        Optional<Slot> found = slotDao.findByDateAndTimeAndTheme(date.plusDays(1), 1L, 1L);

        assertThat(found).isEmpty();
    }

    @Test
    void id로_슬롯을_조회한다() {
        Optional<Slot> found = slotDao.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getDate()).isEqualTo(date);
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        assertThat(slotDao.findById(999L)).isEmpty();
    }

    @Test
    void 슬롯을_제대로_생성한다() {
        Slot slot = Slot.create(LocalDate.parse("2027-06-01"), reservationTime, theme);

        Long generatedId = slotDao.insert(slot);

        Optional<Slot> found = slotDao.findById(generatedId);
        assertThat(found).isPresent();
        assertThat(found.get().getDate()).isEqualTo(LocalDate.parse("2027-06-01"));
    }

    @Test
    void 동일한_날짜_시간_테마로_중복_슬롯_삽입_시_예외가_발생한다() {
        Slot duplicate = Slot.create(date, reservationTime, theme);

        assertThatThrownBy(() -> slotDao.insert(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 슬롯을_삭제한다() {
        long deleted = slotDao.delete(1L);

        assertThat(deleted).isEqualTo(1L);
        assertThat(slotDao.findById(1L)).isEmpty();
    }

    @Test
    void 대기가_참조하는_슬롯은_삭제할_수_없다() {
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '테스트', '2026-05-15 10:30:00')");

        assertThatThrownBy(() -> slotDao.delete(1L))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(slotDao.findById(1L)).isPresent();
    }
}
