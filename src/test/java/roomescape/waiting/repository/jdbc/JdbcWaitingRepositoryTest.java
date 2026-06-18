package roomescape.waiting.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingWithRank;

@JdbcTest
@ActiveProfiles("jdbc")
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

    JdbcWaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        waitingRepository = new JdbcWaitingRepository(jdbcTemplate);
    }

    @Nested
    @DisplayName("슬롯에 대기를 등록한다")
    class Save {

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
            final Waiting saved = waitingRepository.save(waiting);

            //then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("id로 대기를 삭제한다")
    class DeleteById {

        @Test
        void 존재하는_대기를_id로_삭제하면_TRUE를_반환한다() {
            //given
            ReservationTime time = insertReservationTime("11:00:00");
            Theme theme = insertTheme("링", "공포 테마", "http:~");
            final long savedId = insertWaiting("코로구", NOW.toLocalDate(), time.getId(), theme.getId());

            //when
            final boolean deleted = waitingRepository.deleteById(savedId);

            //then
            assertThat(deleted).isTrue();
        }

        @Test
        void 존재하지_않는_대기를_id로_삭제하면_FALSE를_반환한다() {
            //given
            final long unsavedId = 999L;

            //when
            final boolean deleted = waitingRepository.deleteById(unsavedId);

            //then
            assertThat(deleted).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 대기를 조회한다")
    class FindById {

        @Test
        void 존재하지_않는_대기를_조회하면_empty가_반환된다() {
            //given
            final long unsavedId = 99L;

            //when
            final Optional<Waiting> find = waitingRepository.findById(unsavedId);

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
            final Optional<Waiting> find = waitingRepository.findById(savedId);

            //then
            assertThat(find).isPresent();
        }
    }

    @Nested
    @DisplayName("본인의 현재 이후의 전체 대기 목록을 조회한다")
    class FindAllWithRankByCustomerNameAndReservationDateTimeAfter {

        @Test
        @Sql({"/clear.sql", "/find_own_waitings_after_today_test_data.sql"})
        void 본인의_현재_이후의_전체_대기_목록을_조회한다() {
            //given
            final String customerName = "재키";

            //when
            List<WaitingWithRank> waitingWithRanks = waitingRepository
                .findAllWithRankByCustomerNameAndReservationDateTimeAfter(
                    customerName,
                    NOW
                );

            //then
            assertThat(waitingWithRanks).hasSize(2);
            assertThat(waitingWithRanks).extracting(waitingWithRank ->
                LocalDateTime.of(
                    waitingWithRank.waiting().getReservationDate(),
                    waitingWithRank.waiting().getTime().getStartAt())
            ).allMatch(dateTime -> dateTime.isAfter(NOW));
        }

        @Test
        @Sql({"/clear.sql", "/find_own_waitings_after_today_test_data.sql"})
        void 대기_순위는_같은_슬롯_내_등록_순서로_결정된다() {
            //given
            final String customerName = "재키";

            //when
            List<WaitingWithRank> waitingWithRanks = waitingRepository
                .findAllWithRankByCustomerNameAndReservationDateTimeAfter(
                    customerName,
                    NOW
                );

            //then
            WaitingWithRank rankTwo = waitingWithRanks.stream()
                .filter(wr ->
                    wr.waiting().getReservationDate().equals(LocalDate.of(2026, 5, 8)))
                .findFirst()
                .orElseThrow();

            assertThat(rankTwo.rank()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("슬롯(날짜, 시간, 테마) 정보로 대기가 존재하는지 확인한다")
    class ExistsBySlot {

        @Test
        void 동일한_슬롯_정보의_대기가_존재하면_TRUE_를_반환한다() {
            // given
            insertReservationTime("10:00:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertWaiting("수달", date, 1L, 1L);

            // when
            final boolean existsBySlot = waitingRepository.existsBySlot(date, 1L, 1L);

            // then
            assertThat(existsBySlot).isTrue();
        }

        @Test
        void 날짜가_달라_동일한_슬롯_정보의_대기가_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertWaiting("브라운", date, 1L, 1L);

            final LocalDate otherDate = date.plusDays(1);

            // when
            final boolean existsBySlot = waitingRepository.existsBySlot(otherDate, 1L, 1L);

            // then
            assertThat(existsBySlot).isFalse();
        }

        @Test
        void 시간이_달라_동일한_슬롯_정보의_대기가_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00:00");
            insertReservationTime("12:00:00");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertWaiting("브라운", date, 1L, 1L);

            // when
            final boolean existsBySlot = waitingRepository.existsBySlot(date, 2L, 1L);

            // then
            assertThat(existsBySlot).isFalse();
        }

        @Test
        void 테마가_달라_동일한_슬롯_정보의_대기가_존재하지_않으면_FALSE_를_반환한다() {
            // given
            insertReservationTime("10:00:00");
            insertTheme("링", "공포 테마", "http:~");
            insertTheme("링", "공포 테마", "http:~");

            final LocalDate date = LocalDate.parse("2026-08-05");
            insertWaiting("브라운", date, 1L, 1L);

            // when
            final boolean existsBySlot = waitingRepository.existsBySlot(date, 1L, 2L);

            // then
            assertThat(existsBySlot).isFalse();
        }
    }

    private ReservationTime insertReservationTime(final String startAt) {
        jdbcTemplate.update("""
                INSERT INTO reservation_time (start_at) VALUES (?)
                """,
            startAt
        );
        return ReservationTime.of(1L, Time.valueOf(startAt).toLocalTime());
    }

    private Theme insertTheme(final String name, final String description, final String thumbnailUrl) {
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)
                """,
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
        jdbcTemplate.update("""
                INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)
                """,
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
        return keyHolder.getKey().longValue();
    }
}
