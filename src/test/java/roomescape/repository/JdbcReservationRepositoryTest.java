package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingOrder;
import roomescape.domain.ReservationWithWaitingOrder;

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
                new Reservation(null, "브라운", DATE, time, theme, ReservationStatus.CONFIRMED));

        assertThat(saved.id()).isNotNull();
        assertThat(saved.reserverName()).isEqualTo("브라운");
        assertThat(saved.status()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("모든 예약을 조회한다")
    void findAll() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);
        insertReservation("리사", DATE, time, theme);

        List<ReservationWithWaitingOrder> reservations = reservationRepository.findAllActive();

        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("취소된 예약은 전체 조회에서 제외된다")
    void findAllExcludesCanceled() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme);
        Reservation canceled = insertReservation("리사", DATE, time, theme,
                ReservationStatus.CANCELED, Instant.now());

        List<ReservationWithWaitingOrder> reservations = reservationRepository.findAllActive();

        assertThat(reservations)
                .extracting(ReservationWithWaitingOrder::id)
                .doesNotContain(canceled.getId());
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
        reservationRepository.updateAndRequeue(
                new Reservation(saved.getId(), "브라운", newDate, time, theme, ReservationStatus.CONFIRMED));

        Reservation updated = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("취소(soft delete)하면 행은 남고 상태가 CANCELED가 된다")
    void cancel() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Reservation saved = insertReservation("브라운", DATE, time, theme);

        reservationRepository.cancel(saved.getId());

        Reservation canceled = reservationRepository.findById(saved.getId()).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("슬롯에 활성 확정 예약이 있는지 확인한다")
    void existsActiveConfirmed() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme, ReservationStatus.CONFIRMED, Instant.now());

        assertThat(reservationRepository.existsActiveConfirmed(DATE, time.getId(), theme.getId())).isTrue();
        assertThat(reservationRepository.existsActiveConfirmed(
                LocalDate.of(2099, 1, 1), time.getId(), theme.getId())).isFalse();
    }

    @Test
    @DisplayName("확정 취소 후 첫 대기자를 승급한다")
    void promoteEarliestWaiting() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        insertReservation("루드비코", DATE, time, theme, ReservationStatus.WAITING, base.plusSeconds(1));
        Reservation earlier = insertReservation("모아", DATE, time, theme, ReservationStatus.WAITING, base);

        boolean promoted = reservationRepository.promoteEarliestWaiting(DATE, time.getId(), theme.getId());

        assertThat(promoted).isTrue();
        assertThat(reservationRepository.findById(earlier.getId()).orElseThrow().getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
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
    @DisplayName("취소된 예약은 중복 검사에서 제외된다")
    void existsByReserverNameExcludesCanceled() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        insertReservation("브라운", DATE, time, theme, ReservationStatus.CANCELED, Instant.now());

        assertThat(reservationRepository.existsByReserverNameAndDateAndTimeIdAndThemeId(
                "브라운", DATE, time.getId(), theme.getId())).isFalse();
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
    @DisplayName("확정은 0번, 대기는 앞에 있는 활성 예약 수만큼 순번을 부여한다")
    void findByReserverName() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation first = insertReservation("루드비코", DATE, time, theme, ReservationStatus.CONFIRMED, base);
        Reservation second = insertReservation("모아", DATE, time, theme, ReservationStatus.WAITING, base.plusSeconds(1));
        Reservation third = insertReservation("브라운", DATE, time, theme, ReservationStatus.WAITING, base.plusSeconds(2));

        assertThat(reservationRepository.findByReserverName("루드비코"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ReservationWithWaitingOrder(
                        first.getId(), "루드비코", DATE, time, theme, ReservationStatus.CONFIRMED, new WaitingOrder(0)));

        assertThat(reservationRepository.findByReserverName("모아"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ReservationWithWaitingOrder(
                        second.getId(), "모아", DATE, time, theme, ReservationStatus.WAITING, new WaitingOrder(1)));

        assertThat(reservationRepository.findByReserverName("브라운"))
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(new ReservationWithWaitingOrder(
                        third.getId(), "브라운", DATE, time, theme, ReservationStatus.WAITING, new WaitingOrder(2)));
    }

    @Test
    @DisplayName("예약 취소시 대기 순번이 줄어든다")
    void 예약_취소시_대기_순번이_줄어든다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now();
        Reservation confirmed = insertReservation("루드비코", DATE, time, theme, ReservationStatus.CONFIRMED, base);
        insertReservation("모아", DATE, time, theme, ReservationStatus.WAITING, base.plusSeconds(1));
        insertReservation("브라운", DATE, time, theme, ReservationStatus.WAITING, base.plusSeconds(2));

        long before = reservationRepository.findByReserverName("브라운").getFirst().waitingOrder().value();
        assertThat(before).isEqualTo(2L);

        reservationRepository.cancel(confirmed.getId());

        long after = reservationRepository.findByReserverName("브라운").getFirst().waitingOrder().value();
        assertThat(after).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 사람이 선점한 슬롯으로 예약을 변경하면 대기열 맨 뒤(대기)로 들어간다")
    void 예약_변경_시_대기열_맨_뒤로_들어간다() {
        Theme theme = insertTheme("무인도 탈출");
        ReservationTime time11 = insertTime(LocalTime.of(11, 0));
        ReservationTime time13 = insertTime(LocalTime.of(13, 0));
        Instant now = Instant.now();

        Reservation user1 = insertReservation("user1", DATE, time11, theme,
                ReservationStatus.CONFIRMED, now.minusSeconds(10));
        insertReservation("user2", DATE, time13, theme, ReservationStatus.CONFIRMED, now.minusSeconds(5));

        reservationRepository.updateAndRequeue(new Reservation(user1.getId(), "user1", DATE, time13, theme,
                ReservationStatus.WAITING));

        long user2Order = reservationRepository.findByReserverName("user2").getFirst().waitingOrder().value();
        long user1Order = reservationRepository.findByReserverName("user1").getFirst().waitingOrder().value();
        assertThat(user2Order).isEqualTo(0L);
        assertThat(user1Order).isEqualTo(1L);
    }

    @Test
    @DisplayName("enqueued_at이 동률이면 id가 작은 대기가 먼저 순번을 받는다")
    void enqueued_at_동률이면_id로_순번을_가른다() {
        ReservationTime time = insertTime(LocalTime.of(10, 0));
        Theme theme = insertTheme("무인도 탈출");
        Instant base = Instant.now().minusSeconds(10);

        insertReservation("confirmed", DATE, time, theme, ReservationStatus.CONFIRMED, base);
        Instant sameInstant = base.plusSeconds(1);
        Reservation first = insertReservation("user1", DATE, time, theme, ReservationStatus.WAITING, sameInstant);
        Reservation second = insertReservation("user2", DATE, time, theme, ReservationStatus.WAITING, sameInstant);

        long firstOrder = reservationRepository.findByReserverName("user1").getFirst().waitingOrder().value();
        long secondOrder = reservationRepository.findByReserverName("user2").getFirst().waitingOrder().value();

        assertThat(first.getId()).isLessThan(second.getId());
        assertThat(firstOrder).isEqualTo(1L);
        assertThat(secondOrder).isEqualTo(2L);
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
        return insertReservation(name, date, time, theme, ReservationStatus.CONFIRMED, Instant.now());
    }

    private Reservation insertReservation(String name, LocalDate date, ReservationTime time, Theme theme,
                                          ReservationStatus status, Instant enqueuedAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id, status, enqueued_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                name, date.toString(), time.getId(), theme.getId(), status.name(), Timestamp.from(enqueuedAt)
        );
        long id = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
        return new Reservation(id, name, date, time, theme, status);
    }
}
