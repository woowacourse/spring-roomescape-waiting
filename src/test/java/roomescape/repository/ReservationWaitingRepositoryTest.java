package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.domain.WaitingWithTurn;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationWaitingRepository waitingRepository;

    private final LocalDate date = LocalDate.of(2023, 8, 5);

    @BeforeEach
    void setup() {
        this.waitingRepository = new ReservationWaitingRepository(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약대기_추가_테스트() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");
        ReservationWaiting waiting = new ReservationWaiting(null, "브라운", new ReservationSlot(date, time, theme));

        // when
        ReservationWaiting saved = waitingRepository.insert(waiting);

        // then
        ReservationWaiting savedWaiting = waitingRepository.findById(saved.getId()).get();
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(savedWaiting.getName()).isEqualTo(waiting.getName()),
                () -> assertThat(savedWaiting.getSlot().getDate()).isEqualTo(waiting.getSlot().getDate()),
                () -> assertThat(savedWaiting.getSlot().getTime().getStartAt()).isEqualTo(waiting.getSlot().getTime().getStartAt()));
    }

    @Test
    void 예약대기_삭제_테스트() {
        // given
        ReservationTime time1 = findTimeByStartAt("15:00");
        Theme theme1 = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        ReservationTime time2 = findTimeByStartAt("12:00");
        Theme theme2 = new Theme(2L, "테마 이름2", "테마 설명2", "썸네일2");
        ReservationWaiting waiting1 = new ReservationWaiting(null, "브라운", new ReservationSlot(date, time1, theme1));
        ReservationWaiting waiting2 = new ReservationWaiting(null, "구구", new ReservationSlot(date, time2, theme2));
        Long id1 = waitingRepository.insert(waiting1).getId();
        waitingRepository.insert(waiting2);

        // when
        waitingRepository.delete(id1);

        // then
        assertThat(waitingRepository.findById(id1)).isEmpty();
    }

    @Test
    void 이름에_해당하는_예약대기_목록을_조회한다() {
        // given
        ReservationTime time1 = findTimeByStartAt("15:00");
        ReservationTime time2 = findTimeByStartAt("12:00");
        Theme theme1 = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        Theme theme2 = new Theme(2L, "테마 이름2", "테마 설명2", "썸네일2");
        waitingRepository.insert(new ReservationWaiting(null, "구구", new ReservationSlot(date, time1, theme1)));
        waitingRepository.insert(new ReservationWaiting(null, "브라운", new ReservationSlot(date, time1, theme1)));
        waitingRepository.insert(new ReservationWaiting(null, "브라운", new ReservationSlot(date.plusDays(1), time2, theme2)));

        // when
        List<WaitingWithTurn> result = waitingRepository.findByNameWithTurn("브라운");

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(waitingWithTurn -> waitingWithTurn.waiting().getName())
                        .containsExactly("브라운", "브라운"),
                () -> assertThat(result).extracting(waitingWithTurn -> waitingWithTurn.waiting().getSlot().getDate())
                        .containsExactly(date, date.plusDays(1)),
                () -> assertThat(result).extracting(WaitingWithTurn::turn)
                        .containsExactly(2L, 1L));
    }

    @Test
    void 중복된_예약_대기가_존재하는지_확인한다() {
        // given
        String name = "브라운";
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        waitingRepository.insert(new ReservationWaiting(null, name, new ReservationSlot(date, time, theme)));

        // when
        boolean result = waitingRepository.existsByNameAndSlot(name, new ReservationSlot(date, time, theme));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 같은_사용자는_같은_슬롯에_중복_대기를_등록할_수_없다() {
        // given
        String name = "브라운";
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        ReservationWaiting waiting = new ReservationWaiting(null, name, new ReservationSlot(date, time, theme));
        waitingRepository.insert(waiting);

        // when & then
        assertThatThrownBy(() -> waitingRepository.insert(waiting))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 아이디로_예약_대기와_순번을_조회한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        Theme theme = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        waitingRepository.insert(new ReservationWaiting(null, "브라운", new ReservationSlot(date, time, theme)));
        Long id2 = waitingRepository.insert(new ReservationWaiting(null, "구구", new ReservationSlot(date, time, theme))).getId();

        // when
        WaitingWithTurn result = waitingRepository.findByIdWithTurn(id2).get();

        // then
        assertAll(
                () -> assertThat(result.waiting().getId()).isEqualTo(id2),
                () -> assertThat(result.waiting().getName()).isEqualTo("구구"),
                () -> assertThat(result.turn()).isEqualTo(2L));
    }

    @Test
    void 슬롯의_첫번째_예약_대기를_조회한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        ReservationTime otherTime = findTimeByStartAt("12:00");
        Theme theme = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        ReservationSlot slot = new ReservationSlot(date, time, theme);
        ReservationWaiting firstWaiting = waitingRepository.insert(new ReservationWaiting(null, "브라운", slot));
        waitingRepository.insert(new ReservationWaiting(null, "구구", slot));
        waitingRepository.insert(new ReservationWaiting(null, "도라", new ReservationSlot(date, otherTime, theme)));

        // when
        ReservationWaiting result = waitingRepository.findFirstBySlotForUpdate(slot).get();

        // then
        assertAll(
                () -> assertThat(result.getId()).isEqualTo(firstWaiting.getId()),
                () -> assertThat(result.getName()).isEqualTo("브라운"),
                () -> assertThat(result.getSlot().getDate()).isEqualTo(slot.getDate()),
                () -> assertThat(result.getSlot().getTime().getId()).isEqualTo(slot.getTime().getId()),
                () -> assertThat(result.getSlot().getTheme().getId()).isEqualTo(slot.getTheme().getId()));
    }

    @Test
    void 슬롯에_예약_대기가_없으면_빈_값을_반환한다() {
        // given
        ReservationTime time = findTimeByStartAt("15:00");
        ReservationTime otherTime = findTimeByStartAt("12:00");
        Theme theme = new Theme(1L, "테마 이름1", "테마 설명1", "썸네일1");
        waitingRepository.insert(new ReservationWaiting(null, "브라운", new ReservationSlot(date, otherTime, theme)));

        // when
        boolean result = waitingRepository.findFirstBySlotForUpdate(new ReservationSlot(date, time, theme)).isEmpty();

        // then
        assertThat(result).isTrue();
    }

    private ReservationTime findTimeByStartAt(String startAt) {
        String sql = "SELECT id, start_at FROM reservation_time WHERE start_at = ?;";
        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, rowNum) -> {
                    ReservationTime reservationTime = new ReservationTime(
                            resultSet.getLong("id"),
                            resultSet.getObject("start_at", LocalTime.class));
                    return reservationTime;
                }, startAt);
    }

}
