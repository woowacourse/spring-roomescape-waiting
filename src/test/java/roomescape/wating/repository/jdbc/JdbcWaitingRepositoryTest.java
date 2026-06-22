package roomescape.wating.repository.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.exception.UnprocessableContentException;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.dto.WaitingWithRank;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql("/clear.sql")
@Import(JdbcWaitingRepository.class)
class JdbcWaitingRepositoryTest {

    private static final LocalDateTime NOW = LocalDate.now().atTime(10, 30);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    JdbcWaitingRepository jdbcWaitingRepository;

    @Test
    @DisplayName("예약이 있는 슬롯에 대기를 등록할 수 있다")
    void registerWaitingInSlotWithReservation() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
        Long slotId = insertReservation("브라운", tomorrow, time.getId(), theme.getId());
        Waiting waiting = Waiting.create(
                "코로구",
                "customer@example.com",
                ReservationSlot.of(slotId, tomorrow, time, theme),
                NOW
        );

        //when
        final Long savedWaitingId = jdbcWaitingRepository.save(waiting);

        //then
        assertThat(savedWaitingId).isNotNull();
    }

    @Test
    @DisplayName("예약이 없는 슬롯에 대기를 등록하면 예외가 발생한다")
    void throwExceptionWhenRegisteringWaitingInSlotWithoutReservation() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        Long slotId = insertReservationSlot(NOW.plusDays(1).toLocalDate(), time.getId(), theme.getId());
        Waiting waiting = Waiting.create(
                "코로구",
                "customer@example.com",
                ReservationSlot.of(slotId, NOW.plusDays(1).toLocalDate(), time, theme),
                NOW
        );

        //when & then
        assertThatThrownBy(() -> jdbcWaitingRepository.save(waiting))
                .isInstanceOf(UnprocessableContentException.class)
                .hasMessage("예약이 존재하지 않는 슬롯에는 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("id로 대기를 삭제할 수 있다")
    void deleteWaitingById() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        final long savedId = insertWaiting("코로구", NOW.toLocalDate(), time.getId(), theme.getId());

        //when
        final boolean deleted = jdbcWaitingRepository.deleteById(savedId);

        //then
        assertThat(deleted).isTrue();
    }

    @Nested
    @DisplayName("id로 대기를 조회한다")
    class FindById {

        @Test
        @DisplayName("존재하지 않는 대기를 조회하면 empty가 반환된다")
        void returnEmptyWhenWaitingDoesNotExist() {
            //given
            final long unsavedId = 99L;

            //when
            final Optional<Waiting> find = jdbcWaitingRepository.findById(unsavedId);

            //then
            assertThat(find).isEmpty();
        }

        @Test
        @DisplayName("존재하는 대기를 조회하면 해당 대기가 반환된다")
        void returnWaitingWhenWaitingExists() {
            //given
            ReservationTime time = insertReservationTime("11:00:00");
            Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
            final long savedId = insertWaiting("코로구", NOW.toLocalDate(), time.getId(), theme.getId());

            //when
            final Optional<Waiting> find = jdbcWaitingRepository.findById(savedId);

            //then
            assertThat(find).isPresent();
        }
    }

    @Test
    @Sql("/find_own_waitings_after_today_test_data.sql")
    @DisplayName("본인의 현재 이후의 전체 대기 목록을 조회한다")
    void findOwnWaitingsAfterNow() {
        //given
        final String customerName = "재키";

        //when
        List<WaitingWithRank> waitings = jdbcWaitingRepository.findAllWithRankByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
                customerName,
                "jaekkii@example.com",
                NOW
        );

        //then
        assertThat(waitings).hasSize(2);
        assertThat(waitings).extracting(waitingWithRank ->
                        LocalDateTime.of(
                                waitingWithRank.waiting().getReservationDate(),
                                waitingWithRank.waiting().getTime().getStartAt()
                        ))
                .allMatch(dateTime -> dateTime.isAfter(NOW));

    }

    @Test
    @DisplayName("같은 생성 시각이면 id가 작은 대기를 먼저 조회한다")
    void findWaitingWithSmallerIdFirstWhenCreatedAtIsSame() {
        // given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        final LocalDate reservationDate = NOW.plusDays(1).toLocalDate();
        final LocalDateTime sameCreatedAt = NOW.minusMinutes(1);

        insertWaiting(1L, "코로구", reservationDate, time.getId(), theme.getId(), sameCreatedAt);
        insertWaiting(2L, "재키", reservationDate, time.getId(), theme.getId(), sameCreatedAt);

        // when
        Long slotId = insertReservationSlot(reservationDate, time.getId(), theme.getId());

        Optional<Waiting> waiting = jdbcWaitingRepository.findEarliestBySlotId(slotId);

        // then
        assertThat(waiting).isPresent();
        assertThat(waiting.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 생성 시각이면 id가 작은 대기를 앞선 대기로 계산한다")
    void calculateWaitingWithSmallerIdAsEarlierWhenCreatedAtIsSame() {
        // given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~", 10000);
        final LocalDate reservationDate = NOW.plusDays(1).toLocalDate();
        final LocalDateTime sameCreatedAt = NOW.minusMinutes(1);

        insertWaiting(1L, "코로구", reservationDate, time.getId(), theme.getId(), sameCreatedAt);
        final long targetWaitingId = 2L;
        insertWaiting(targetWaitingId, "재키", reservationDate, time.getId(), theme.getId(), sameCreatedAt);

        // when
        List<WaitingWithRank> waitings = jdbcWaitingRepository.findAllWithRankByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
                "재키",
                emailFromName("재키"),
                NOW
        );

        // then
        assertThat(waitings).extracting(WaitingWithRank::rank)
                .containsExactly(2);
    }

    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(
            final String name,
            final String description,
            final String thumbnailUrl,
            final int price
    ) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                description,
                thumbnailUrl,
                price
        );

        return Theme.of(1L, name, description, thumbnailUrl, price);
    }

    private Long insertReservation(
            final String name,
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        Long slotId = insertReservationSlot(date, timeId, themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation(customer_name, customer_email, slot_id, status) VALUES (?, ?, ?, ?)",
                name,
                emailFromName(name),
                slotId,
                "CONFIRMED"
        );
        return slotId;
    }

    private long insertWaiting(
            final String name,
            final LocalDate reservationDate,
            final long timeId,
            final long themeId
    ) {
        Long slotId = insertReservationSlot(reservationDate, timeId, themeId);
        final String sql = """
                INSERT INTO waiting(customer_name, customer_email, slot_id)
                VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, name);
            ps.setString(2, emailFromName(name));
            ps.setLong(3, slotId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
    }

    private void insertWaiting(
            final long id,
            final String name,
            final LocalDate reservationDate,
            final long timeId,
            final long themeId,
            final LocalDateTime createdAt
    ) {
        Long slotId = insertReservationSlot(reservationDate, timeId, themeId);
        final String sql = """
                INSERT INTO waiting(id, customer_name, customer_email, slot_id, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                id,
                name,
                emailFromName(name),
                slotId,
                Timestamp.valueOf(createdAt)
        );
    }

    private String emailFromName(final String name) {
        return "customer" + Math.abs(name.hashCode()) + "@example.com";
    }

    private Long insertReservationSlot(final LocalDate reservationDate, final long timeId, final long themeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO reservation_slot(reservation_date, time_id, theme_id) VALUES (?, ?, ?)",
                    Date.valueOf(reservationDate),
                    timeId,
                    themeId
            );
        } catch (DuplicateKeyException ignored) {
        }
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_slot WHERE reservation_date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                Date.valueOf(reservationDate),
                timeId,
                themeId
        );
    }
}
