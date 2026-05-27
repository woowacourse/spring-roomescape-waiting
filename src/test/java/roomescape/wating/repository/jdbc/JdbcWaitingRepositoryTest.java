package roomescape.wating.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.Waiting;
import roomescape.wating.domain.exception.NoReservationForWaitingException;

@JdbcTest
@Sql("/clear.sql")
class JdbcWaitingRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    ));

    @Autowired
    JdbcTemplate jdbcTemplate;

    JdbcWaitingRepository jdbcWaitingRepository;

    @BeforeEach
    void setUp() {
        jdbcWaitingRepository = new JdbcWaitingRepository(jdbcTemplate);
    }

    @Test
    void 예약이_있는_슬롯에_대기를_등록할_수_있다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");
        final LocalDate tomorrow = NOW.plusDays(1).toLocalDate();
        insertReservation("브라운", tomorrow, time.getId(), theme.getId());
        Waiting waiting = Waiting.create(
                "코로구",
                tomorrow,
                time,
                theme,
                NOW
        );

        //when
        final Long savedWaitingId = jdbcWaitingRepository.save(waiting);

        //then
        assertThat(savedWaitingId).isNotNull();
    }

    @Test
    void 예약이_없는_슬롯에_대기를_등록하면_예외가_발생한다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");
        Waiting waiting = Waiting.create(
                "코로구",
                NOW.plusDays(1).toLocalDate(),
                time,
                theme,
                NOW
        );

        //when & then
        assertThatThrownBy(() -> jdbcWaitingRepository.save(waiting))
                .isInstanceOf(NoReservationForWaitingException.class);
    }

    @Test
    void id로_대기를_삭제할_수_있다() {
        //given
        ReservationTime time = insertReservationTime("11:00:00");
        Theme theme = insertTheme("링", "공포 테마", "http:~");
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
        void 존재하지_않는_대기를_조회하면_empty가_반환된다() {
            //given
            final long unsavedId = 99L;

            //when
            final Optional<Waiting> find = jdbcWaitingRepository.findById(unsavedId);

            //then
            assertThat(find).isEmpty();
        }

        @Test
        void 존재하는_대기를_조회하면_해당_대기가_반환된다() {
            //given
            ReservationTime time = insertReservationTime("11:00:00");
            Theme theme = insertTheme("링", "공포 테마", "http:~");
            final long savedId = insertWaiting("코로구", NOW.toLocalDate(), time.getId(), theme.getId());

            //when
            final Optional<Waiting> find = jdbcWaitingRepository.findById(savedId);

            //then
            assertThat(find).isPresent();
        }
    }

    @Test
    @Sql("/find_own_waitings_after_today_test_data.sql")
    void 본인의_현재_이후의_전체_대기_목록을_조회한다() {
        //given
        final String customerName = "재키";

        //when
        List<Waiting> waitings = jdbcWaitingRepository.findAllByCustomerNameAndReservationDateTimeAfter(
                customerName,
                NOW
        );

        //then
        assertThat(waitings).hasSize(2);
        assertThat(waitings).extracting(waiting ->
                        LocalDateTime.of(waiting.getReservationDate(), waiting.getTime().getStartAt()))
                .allMatch(dateTime -> dateTime.isAfter(NOW));

    }

    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name,
                description,
                thumbnailUrl
        );

        return Theme.of(1L, name, description, thumbnailUrl);
    }

    private void insertReservation(
            final String name,
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name,
                Date.valueOf(date),
                timeId,
                themeId
        );
    }

    private long insertWaiting(
            final String name,
            final LocalDate reservationDate,
            final long timeId,
            final long themeId
    ) {
        final String sql = """
                INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, name);
            ps.setDate(2, Date.valueOf(reservationDate));
            ps.setLong(3, timeId);
            ps.setLong(4, themeId);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
    }
}
