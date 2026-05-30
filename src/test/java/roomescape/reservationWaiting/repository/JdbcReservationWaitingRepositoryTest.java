package roomescape.reservationWaiting.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@JdbcTest
class JdbcReservationWaitingRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    public JdbcReservationWaitingRepositoryTest(JdbcTemplate jdbcTemplate) {
        this.reservationWaitingRepository = new JdbcReservationWaitingRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 대기를 저장하고 반환된 객체의 ID를 확인한다.")
    void save_validWaiting_returnsWithId() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        // when
        ReservationWaiting saved1 = reservationWaitingRepository.save(
                ReservationWaiting.of(
                        "브라운",
                        LocalDate.of(2024, 5, 1),
                        time,
                        theme
                )
        );

        ReservationWaiting saved2 = reservationWaitingRepository.save(
                ReservationWaiting.of(
                        "포비",
                        LocalDate.of(2024, 5, 1),
                        time,
                        theme
                )
        );

        // then
        assertThat(saved1.id()).isNotNull();
        assertThat(saved2.id()).isNotNull();
    }

    @Test
    @DisplayName("기존에 이미 동일한 예약 대기가 있으면 예외가 발생한다.")
    void save_duplicateWaiting_throwsDataIntegrityViolation() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        reservationWaitingRepository.save(
                ReservationWaiting.of(
                        "브라운",
                        LocalDate.of(2024, 5, 1),
                        time,
                        theme
                )
        );

        // when & then
        assertThatThrownBy(() -> reservationWaitingRepository.save(
                ReservationWaiting.of(
                        "브라운",
                        LocalDate.of(2024, 5, 1),
                        time,
                        theme
                )
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private ReservationTime createTime(LocalTime time) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(time)
        );

        long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class,
                Time.valueOf(time)
        );

        return new ReservationTime(timeId, time);
    }

    private Theme createTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, description, thumbnailUrl
        );

        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                name
        );

        return new Theme(themeId, name, description, thumbnailUrl);
    }



    @Test
    @DisplayName("날짜, 시간, 예약자 이름에 해당하는 예약 대기가 존재하는지 조회한다.")
    void existsByDateAndTimeIdAndAndName_matchingEntry_returnsTrue() {
        //given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");
        ReservationWaiting saved = saveReservationWaiting("브라운", LocalDate.of(2024, 5, 1), time, theme);

        //when & then
        assertThat(reservationWaitingRepository.existsByDateAndTimeIdAndName(
                saved.date(),
                saved.time().id(),
                "브라운"
        )).isTrue();

        assertThat(reservationWaitingRepository.existsByDateAndTimeIdAndName(
                saved.date(),
                saved.time().id(),
                "포비"
        )).isFalse();
    }

    @Test
    @DisplayName("아이디를 기반으로 예약 대기를 삭제한다")
    void deleteById_success() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        ReservationWaiting saved = saveReservationWaiting("브라운", LocalDate.of(2024, 5, 1), time, theme);

        // when
        int deletedCount = reservationWaitingRepository.deleteById(saved.id());

        // then
        assertThat(deletedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("아이디를 기반으로 예약 대기를 잘 찾는다")
    void findById_success() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        ReservationWaiting saved = saveReservationWaiting("브라운", LocalDate.of(2024, 5, 1), time, theme);

        // when
        Optional<ReservationWaiting> found = reservationWaitingRepository.findById(saved.id());

        // then
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("이름을 기반으로 예약 대기 전부 찾아 온다")
    void findAllByName() {
        // given
        ReservationTime time = createTime(LocalTime.of(10, 0));
        Theme theme = createTheme("우테코", "우테코 전용 테마", "https://example.com");

        ReservationWaiting saved1 = saveReservationWaiting("브라운", LocalDate.of(2024, 5, 1), time, theme);
        saveReservationWaiting("검프", LocalDate.of(2024, 5, 1), time, theme);
        saveReservationWaiting("포비", LocalDate.of(2024, 5, 1), time, theme);

        // when
        List<ReservationWaiting> result = reservationWaitingRepository.findAllByName("브라운");

        // then
        assertThat(result).containsExactly(saved1);
    }

    private ReservationWaiting saveReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        return reservationWaitingRepository.save(
                ReservationWaiting.of(name, date, time, theme)
        );
    }

}
