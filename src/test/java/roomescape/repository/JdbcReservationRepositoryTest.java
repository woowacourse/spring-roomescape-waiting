package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.ReservationWithWaitingOrder;

@JdbcTest
@Import(JdbcReservationRepository.class)
@Sql(scripts = "/schema.sql")
class JdbcReservationRepositoryTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("예약을 저장하면 id가 채번되어 반환된다")
    void save() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");

        ReservationWithWaitingOrder saved = reservationRepository.save(
                new Reservation(null, "브라운", DATE, time, theme));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("모든 예약을 조회한다")
    void findAll() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);
        insertReservation("리사", DATE, time, theme);

        List<ReservationWithWaitingOrder> reservations = reservationRepository.findAll();

        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("id로 예약을 조회한다")
    void findById() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        assertThat(reservationRepository.findById(saved.getId())).isPresent();
        assertThat(reservationRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("예약을 수정한다")
    void update() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        LocalDate newDate = LocalDate.of(2099, 1, 1);
        reservationRepository.update(
                new Reservation(saved.getId(), "브라운", newDate, time, theme));

        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("id로 예약을 삭제한다")
    void deleteById() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        reservationRepository.deleteById(saved.getId());

        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("id 존재 여부를 확인한다")
    void existsById() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        assertThat(reservationRepository.existsById(saved.getId())).isTrue();
        assertThat(reservationRepository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("날짜+시간+테마 조합의 예약 존재 여부를 확인한다")
    void existsByNameAndDateAndTimeIdAndThemeId() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);

        assertThat(
                reservationRepository.existsByNameAndDateAndTimeIdAndThemeId("브라운", DATE, time.getId(), theme.getId()))
                .isTrue();
        assertThat(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(
                "브라운", LocalDate.of(2099, 1, 1), time.getId(), theme.getId())).isFalse();
    }

    @Test
    @DisplayName("특정 예약을 제외한 날짜+시간+테마 조합의 예약 존재 여부를 확인한다")
    void existsByNameAndDateAndTimeIdAndThemeIdAndIdNot() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                DATE, time.getId(), theme.getId(), saved.getId())).isFalse();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeIdAndIdNot(
                DATE, time.getId(), theme.getId(), 999L)).isTrue();
    }

    @Test
    @DisplayName("시간을 사용하는 예약 존재 여부를 확인한다")
    void existsByTimeId() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);

        assertThat(reservationRepository.existsByTimeId(time.getId())).isTrue();
        assertThat(reservationRepository.existsByTimeId(999L)).isFalse();
    }

    @Test
    @DisplayName("테마를 사용하는 예약 존재 여부를 확인한다")
    void existsByThemeId() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);

        assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue();
        assertThat(reservationRepository.existsByThemeId(999L)).isFalse();
    }

    @Test
    @DisplayName("앞에 존재하는 예약 수에 따라 적절한 대기 순번을 부여한다")
    void findByName() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation first = insertReservationWithCreatedAt("루드비코", DATE, time, theme, base);
        Reservation second = insertReservationWithCreatedAt("모아", DATE, time, theme, base.plusSeconds(1));
        Reservation third = insertReservationWithCreatedAt("브라운", DATE, time, theme, base.plusSeconds(2));

        assertThat(reservationRepository.findByName("루드비코"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(first.getId(), "루드비코", DATE, time, theme, 0L));

        assertThat(reservationRepository.findByName("모아"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(second.getId(), "모아", DATE, time, theme, 1L));

        assertThat(reservationRepository.findByName("브라운"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(third.getId(), "브라운", DATE, time, theme, 2L));
    }

    @Test
    @DisplayName("같은 사용자는 같은 슬롯에 중복 대기할 수 없다")
    void 중복_예약을_시도하면_예외를_던진다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        insertReservationWithCreatedAt("루드비코", DATE, time, theme, base);

        assertThatThrownBy(() -> insertReservationWithCreatedAt("루드비코", DATE, time, theme, base))
                .isExactlyInstanceOf(DuplicateKeyException.class);

    }

    @Test
    @DisplayName("예약 취소시 대기 순번이 줄어든다")
    void 예약_취소시_대기_순번이_줄어든다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation reservation1 = insertReservationWithCreatedAt("루드비코", DATE, time, theme, base);
        insertReservationWithCreatedAt("모아", DATE, time, theme, base.plusSeconds(1));
        insertReservationWithCreatedAt("브라운", DATE, time, theme, base.plusSeconds(2));

        List<ReservationWithWaitingOrder> list = reservationRepository.findByName("브라운");
        Long waitingOrder = list.getFirst().waitingOrder();
        assertThat(waitingOrder).isEqualTo(2L);

        reservationRepository.deleteById(reservation1.getId());
        assertThat(reservationRepository.findById(reservation1.getId())).isEmpty();

        List<ReservationWithWaitingOrder> list2 = reservationRepository.findByName("브라운");
        Long waitingOrder2 = list2.getFirst().waitingOrder();
        assertThat(waitingOrder2).isEqualTo(1L);

    }

    private ReservationTime insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt.toString());
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
        return new ReservationTime(id, startAt);
    }

    private Theme insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "https://example.com/thumb.jpg"
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
        return new Theme(id, name, "설명", "https://example.com/thumb.jpg");
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date.toString(), time.getId(), theme.getId()
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        return new Reservation(id, name, date, time, theme);
    }

    private Reservation insertReservationWithCreatedAt(String name, LocalDate date, ReservationTime time, Theme theme,
                                                       Instant createdAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)",
                name, date.toString(), time.getId(), theme.getId(), Timestamp.from(createdAt)
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        return new Reservation(id, name, date, time, theme);
    }
}
