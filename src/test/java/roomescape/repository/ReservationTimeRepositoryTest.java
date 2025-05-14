package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.domain.ReservationTime;

@JdbcTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@Disabled
class ReservationTimeRepositoryTest {

    ReservationTimeRepository timeRepository;

    @Autowired
    JdbcTemplate template;

    @BeforeEach
    void setUp() {

        timeRepository = new ReservationTimeRepositoryImpl(null,template);
        template.execute("DELETE FROM reservation");
        template.execute("DELETE FROM reservation_time");
        template.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        template.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        template.execute("insert into reservation_time (start_at) values ('15:40')");

    }

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findById() {
        //when
        ReservationTime time = timeRepository.findById(1L).get();

        //then
        assertThat(time.getStartAt()).isEqualTo(LocalTime.parse("15:40"));
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAll() {
        //when
        List<ReservationTime> times = timeRepository.findAll();

        //then
        ReservationTime time = times.getFirst();
        assertThat(times).hasSize(1);
        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.parse("15:40"));
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void save() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(16, 30));

        //when
        ReservationTime saved = timeRepository.save(reservationTime);
        ReservationTime firstTime = timeRepository.findById(1L).get();
        ReservationTime secondTime = timeRepository.findById(2L).get();

        //then
        assertThat(saved.getId()).isEqualTo(2L);
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.parse("16:30"));
        assertThat(firstTime.getId()).isEqualTo(1L);
        assertThat(firstTime.getStartAt()).isEqualTo(LocalTime.parse("15:40"));
        assertThat(secondTime.getId()).isEqualTo(2L);
        assertThat(secondTime.getStartAt()).isEqualTo(LocalTime.parse("16:30"));
    }

    @DisplayName("id로 예약시간을 삭제한다.")
    @Test
    void deleteById() {
        //when
        int deleteCounts = timeRepository.deleteById(1L);

        //then
        assertThat(deleteCounts).isEqualTo(1);
        assertThat(timeRepository.findAll()).isEmpty();
    }

    @DisplayName("이미 존재하는 예약 시간이므로 true를 반환한다.")
    @Test
    void existByStartAt() {
        //given
        LocalTime startAt = LocalTime.of(16, 30);
        ReservationTime reservationTime = new ReservationTime(startAt);
        timeRepository.save(reservationTime);

        //when
        final boolean expected = timeRepository.existsByStartAt(LocalTime.of(16,30));

        //then
        assertThat(expected).isTrue();
    }
}
