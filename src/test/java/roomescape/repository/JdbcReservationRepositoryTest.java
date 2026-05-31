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
import roomescape.domain.WaitingOrder;
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
        assertThat(saved.reserverName()).isEqualTo("브라운");
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
    void existsByReserverNameAndDateAndTimeIdAndThemeId() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);

        assertThat(
                reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId("브라운", DATE, time.getId(), theme.getId()))
                .isTrue();
        assertThat(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                "브라운", LocalDate.of(2099, 1, 1), time.getId(), theme.getId())).isFalse();
    }

    @Test
    @DisplayName("자기 자신을 제외하고 본인이 같은 슬롯에 예약/대기 중인지 확인한다")
    void existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation mine = insertReservation("브라운", DATE, time, theme);
        insertReservation("모아", DATE, time, theme); // 타인은 같은 슬롯에 있어도 본인 기준 판단에 영향 없음

        // 본인(브라운) row를 제외하면 본인의 다른 예약은 없음 → false (모아가 슬롯에 있어도 무관)
        assertThat(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                "브라운", DATE, time.getId(), theme.getId(), mine.getId())).isFalse();
        // 본인 row가 제외 대상이 아니면 → true
        assertThat(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeIdAndIdNot(
                "브라운", DATE, time.getId(), theme.getId(), 999L)).isTrue();
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
    void findByReserverName() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation first = insertReservationWithUpdatedAt("루드비코", DATE, time, theme, base);
        Reservation second = insertReservationWithUpdatedAt("모아", DATE, time, theme, base.plusSeconds(1));
        Reservation third = insertReservationWithUpdatedAt("브라운", DATE, time, theme, base.plusSeconds(2));

        assertThat(reservationRepository.findByReserverName("루드비코"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(first.getId(), "루드비코", DATE, time, theme, new WaitingOrder(0)));

        assertThat(reservationRepository.findByReserverName("모아"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(second.getId(), "모아", DATE, time, theme, new WaitingOrder(1)));

        assertThat(reservationRepository.findByReserverName("브라운"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(
                        new ReservationWithWaitingOrder(third.getId(), "브라운", DATE, time, theme, new WaitingOrder(2)));
    }

    @Test
    @DisplayName("같은 사용자는 같은 슬롯에 중복 대기할 수 없다")
    void 중복_예약을_시도하면_예외를_던진다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        insertReservationWithUpdatedAt("루드비코", DATE, time, theme, base);

        assertThatThrownBy(() -> insertReservationWithUpdatedAt("루드비코", DATE, time, theme, base))
                .isExactlyInstanceOf(DuplicateKeyException.class);

    }

    @Test
    @DisplayName("예약 취소시 대기 순번이 줄어든다")
    void 예약_취소시_대기_순번이_줄어든다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation reservation1 = insertReservationWithUpdatedAt("루드비코", DATE, time, theme, base);
        insertReservationWithUpdatedAt("모아", DATE, time, theme, base.plusSeconds(1));
        insertReservationWithUpdatedAt("브라운", DATE, time, theme, base.plusSeconds(2));

        List<ReservationWithWaitingOrder> list = reservationRepository.findByReserverName("브라운");
        long waitingOrder = list.getFirst().waitingOrder().value();
        assertThat(waitingOrder).isEqualTo(2L);

        reservationRepository.deleteById(reservation1.getId());
        assertThat(reservationRepository.findById(reservation1.getId())).isEmpty();

        List<ReservationWithWaitingOrder> list2 = reservationRepository.findByReserverName("브라운");
        long waitingOrder2 = list2.getFirst().waitingOrder().value();
        assertThat(waitingOrder2).isEqualTo(1L);

    }

    @Test
    @DisplayName("다른 사람이 선점한 슬롯으로 예약을 변경하면 대기열 맨 뒤(대기)로 들어간다")
    void 예약_변경_시_대기열_맨_뒤로_들어간다() {
        Theme theme = insertTheme("무인도 탈출");
        ReservationTime time11 = insertTime(LocalTime.of(11, 0));
        ReservationTime time13 = insertTime(LocalTime.of(13, 0));
        Instant now = Instant.now();

        // user1이 먼저 11시를, user2가 나중에 13시를 예약 (user1.updated_at < user2.updated_at, 둘 다 과거)
        Reservation user1 = insertReservationWithUpdatedAt("user1", DATE, time11, theme, now.minusSeconds(10));
        insertReservationWithUpdatedAt("user2", DATE, time13, theme, now.minusSeconds(5));

        // user1이 user2가 선점한 13시 슬롯으로 변경
        reservationRepository.update(new Reservation(user1.getId(), "user1", DATE, time13, theme));

        // user2는 확정(0번), 나중에 끼어든 user1은 대기(1번) 이어야 한다
        long user2Order = reservationRepository.findByReserverName("user2").getFirst().waitingOrder().value();
        long user1Order = reservationRepository.findByReserverName("user1").getFirst().waitingOrder().value();
        assertThat(user2Order).isEqualTo(0L);
        assertThat(user1Order).isEqualTo(1L);
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
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date.toString(), time.getId(), theme.getId()
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        return new Reservation(id, name, date, time, theme);
    }

    private Reservation insertReservationWithUpdatedAt(String name, LocalDate date, ReservationTime time, Theme theme,
                                                       Instant updatedAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                name, date.toString(), time.getId(), theme.getId(),
                Timestamp.from(updatedAt), Timestamp.from(updatedAt)
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        return new Reservation(id, name, date, time, theme);
    }
}
