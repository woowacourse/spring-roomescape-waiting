package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.domain.reservationWaiting.ReservationWaitingRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;

@JdbcTest
public class ReservationWaitingDaoTest {

    private final static ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final static Theme theme = new Theme(1L, "테스트", "설명", "url");
    private final static Slot slot = Slot.restore(1L, LocalDate.parse("2027-05-27"), reservationTime, theme);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationWaitingRepository reservationWaitingDao;

    @BeforeEach
    void setUp() {
        this.reservationWaitingDao = new JdbcReservationWaitingRepository(jdbcTemplate);

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
        jdbcTemplate.update("insert into reservation (slot_id, name, created_at) values (1, '예약자', '2026-05-01 09:00:00')");
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '테스트', '2026-05-15 10:30:00')");
    }

    @Test
    void 예약_대기가_제대로_존재하는_지_조회한다() {
        assertThat(reservationWaitingDao.isExistByNameAndSlotId("테스트", 1L)).isTrue();
        assertThat(reservationWaitingDao.isExistByNameAndSlotId("없는사람", 1L)).isFalse();
    }

    @Test
    void 예약_대기가_id로_정상_조회한다() {
        assertThat(reservationWaitingDao.findReservationWaitingById(1).isPresent()).isTrue();
        assertThat(reservationWaitingDao.findReservationWaitingById(1).get().getName()).isEqualTo("테스트");
    }

    @Test
    void 예약_대기_전체_조회가_정상_조회한다() {
        assertThat(reservationWaitingDao.findAllReservationWaiting().size()).isEqualTo(1);
    }

    @Test
    void 예약_대기가_이름으로_정상_조회한다() {
        assertThat(reservationWaitingDao.findAllByName("테스트").size()).isEqualTo(1);
        assertThat(reservationWaitingDao.findAllByName("테스트").get(0).getCreatedAt())
                .isEqualTo(LocalDateTime.parse("2026-05-15T10:30:00"));
    }

    @Test
    void 예약_대기를_제대로_생성한다() {
        ReservationWaiting reservationWaiting = ReservationWaiting.create("새사람", slot);

        reservationWaitingDao.create(reservationWaiting);

        Optional<ReservationWaiting> found = reservationWaitingDao.findAllByName("새사람").stream().findFirst();
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getDate()).isEqualTo(LocalDate.parse("2027-05-27"));
    }

    @Test
    void 동일한_이름과_슬롯으로_중복_대기열_삽입_시_예외가_발생한다() {
        ReservationWaiting duplicate = ReservationWaiting.create("테스트", slot);

        assertThatThrownBy(() -> reservationWaitingDao.create(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 예약_대기를_제대로_삭제한다() {
        String sql = "SELECT EXISTS (SELECT 1 FROM waiting WHERE id = ?)";

        assertThat(jdbcTemplate.queryForObject(sql, Boolean.class, 1L)).isTrue();

        reservationWaitingDao.delete(1L);

        assertThat(jdbcTemplate.queryForObject(sql, Boolean.class, 1L)).isFalse();
    }

    @Test
    void 여러_대기열_중_가장_먼저_등록된_대기자를_반환한다() {
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '두번째', '2026-05-16 10:30:00')");

        Optional<ReservationWaiting> first = reservationWaitingDao.findFirstBySlotId(1L);

        assertThat(first).isPresent();
        assertThat(first.get().getName()).isEqualTo("테스트");
        assertThat(first.get().getSequence()).isEqualTo(1L);
    }

    @Test
    void 가첫_번째_대기자를_조회한다() {
        Optional<ReservationWaiting> first = reservationWaitingDao.findFirstBySlotId(1L);

        assertThat(first).isPresent();
        assertThat(first.get().getName()).isEqualTo("테스트");
    }

    @Test
    void 존재하지_않는_슬롯의_대기열_조회시_빈_값을_반환한다() {
        Optional<ReservationWaiting> result = reservationWaitingDao.findFirstBySlotId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void 대기열_순번이_등록_순서대로_계산된다() {
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '두번째', '2026-05-16 10:30:00')");

        List<ReservationWaiting> all = reservationWaitingDao.findAllReservationWaiting();
        ReservationWaiting first = all.stream().filter(w -> w.getName().equals("테스트")).findFirst().orElseThrow();
        ReservationWaiting second = all.stream().filter(w -> w.getName().equals("두번째")).findFirst().orElseThrow();

        assertThat(first.getSequence()).isEqualTo(1L);
        assertThat(second.getSequence()).isEqualTo(2L);
    }

    @Test
    void 이름으로_조회_시_일치하는_항목이_없으면_빈_리스트가_반환된다() {
        assertThat(reservationWaitingDao.findAllByName("없는사람")).isEmpty();
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        assertThat(reservationWaitingDao.findReservationWaitingById(999L)).isEmpty();
    }

    @Test
    void 서로_다른_슬롯의_대기_순번은_각_슬롯_내에서_독립적으로_계산된다() {
        jdbcTemplate.update("insert into slot (date, time_id, theme_id) values ('2027-06-01', 1, 1)");
        jdbcTemplate.update("insert into reservation (slot_id, name, created_at) values (2, '예약자2', '2026-05-01 09:00:00')");
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (2, '다른예약대기1', '2026-05-15 10:30:00')");
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '두번째', '2026-05-16 10:30:00')");

        List<ReservationWaiting> all = reservationWaitingDao.findAllReservationWaiting();
        ReservationWaiting inOtherSlot = all.stream()
                .filter(w -> w.getName().equals("다른예약대기1"))
                .findFirst().orElseThrow();

        assertThat(inOtherSlot.getSequence()).isEqualTo(1L);
    }

    @Test
    void 대기열이_없을_때_전체_조회하면_빈_리스트가_반환된다() {
        jdbcTemplate.update("delete from waiting");

        assertThat(reservationWaitingDao.findAllReservationWaiting()).isEmpty();
    }
}
