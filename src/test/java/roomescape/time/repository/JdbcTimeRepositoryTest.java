package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.time.domain.ReservationTime;

@JdbcTest
@Import(JdbcTimeRepository.class)
class JdbcTimeRepositoryTest {

    @Autowired
    private JdbcTimeRepository jdbcTimeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("시간 슬롯을 저장하면 PK가 부여된 객체를 반환한다.")
    @Test
    void save() {
        // given
        LocalDateTime start = LocalDateTime.of(2030, 6, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 6, 1, 12, 0);

        // when
        ReservationTime saved = jdbcTimeRepository.save(start, end);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(start);
        assertThat(saved.getEndAt()).isEqualTo(end);
    }

    @DisplayName("전체 시간 목록을 id 오름차순으로 반환한다.")
    @Test
    void findAll() {
        // given
        assertThat(jdbcTimeRepository.findAll()).isEmpty();
        ReservationTime first = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        ReservationTime second = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 13, 0),
                LocalDateTime.of(2030, 6, 1, 15, 0));

        // when
        List<ReservationTime> times = jdbcTimeRepository.findAll();

        // then
        assertThat(times).extracting(ReservationTime::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @DisplayName("id로 시간 슬롯을 조회한다.")
    @Test
    void findById_존재하면_반환() {
        // given
        ReservationTime saved = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));

        // when
        Optional<ReservationTime> found = jdbcTimeRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getStartAt()).isEqualTo(saved.getStartAt());
    }

    @DisplayName("존재하지 않는 id로 조회하면 빈 Optional을 반환한다.")
    @Test
    void findById_존재하지_않으면_empty() {
        // when
        Optional<ReservationTime> found = jdbcTimeRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }

    @DisplayName("주어진 날짜에 속한 시간 슬롯만 반환한다.")
    @Test
    void findByDate_해당_날짜만_반환() {
        // given
        ReservationTime sameDay1 = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));
        ReservationTime sameDay2 = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 2, 10, 0),
                LocalDateTime.of(2030, 6, 2, 12, 0));

        // when
        List<ReservationTime> times = jdbcTimeRepository.findByDate(LocalDate.of(2030, 6, 1));

        // then
        assertThat(times).extracting(ReservationTime::getId)
                .containsExactly(sameDay1.getId(), sameDay2.getId());
    }

    @DisplayName("findByDate는 시작 시각 오름차순으로 정렬해 반환한다.")
    @Test
    void findByDate_시작시간_오름차순_정렬() {
        // given
        ReservationTime later = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        ReservationTime earlier = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));

        // when
        List<ReservationTime> times = jdbcTimeRepository.findByDate(LocalDate.of(2030, 6, 1));

        // then
        assertThat(times).extracting(ReservationTime::getId)
                .containsExactly(earlier.getId(), later.getId());
    }

    @DisplayName("id로 시간 슬롯을 삭제한다.")
    @Test
    void deleteById_존재하면_true() {
        // given
        ReservationTime saved = jdbcTimeRepository.save(
                LocalDateTime.of(2030, 6, 1, 10, 0),
                LocalDateTime.of(2030, 6, 1, 12, 0));

        // when
        boolean deleted = jdbcTimeRepository.deleteById(saved.getId());

        // then
        assertThat(deleted).isTrue();
        assertThat(jdbcTimeRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("존재하지 않는 id를 삭제하면 false를 반환한다.")
    @Test
    void deleteById_존재하지_않으면_false() {
        // when
        boolean deleted = jdbcTimeRepository.deleteById(999L);

        // then
        assertThat(deleted).isFalse();
    }
}
