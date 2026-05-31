package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.WaitingListRow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(WaitingListRepository.class)
@Sql(scripts = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WaitingListRepositoryTest {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired WaitingListRepository waitingListRepository;

    private Theme theme;
    private ReservationTime reservationTime;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                "테스트테마", "테스트용 설명입니다", "https://thumbnail.com");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)",
                LocalTime.of(10, 0), LocalTime.of(11, 0));

        theme = Theme.createWithId(1L, "테스트 테마", "테스트용 설명입니다", "https://thumbnail.com");
        reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Nested
    class findById {

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환() {
            // when
            var result = waitingListRepository.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findByName {

        @Test
        void 이름으로_대기목록을_조회() {
            // given
            WaitingList waitingList = WaitingList.create("오리", LocalDate.now().plusDays(1), theme, reservationTime);
            waitingListRepository.save(waitingList);

            // when
            List<WaitingListRow> result = waitingListRepository.findByName("오리");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().waitingList().getName()).isEqualTo("오리");
        }

        @Test
        void 없는_이름이면_빈_목록을_반환() {
            // when
            List<WaitingListRow> result = waitingListRepository.findByName("없는사람");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class existsByNameAndThemeAndDateAndTime {

        @Test
        void 동일한_조건으로_대기중이면_true를_반환() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            WaitingList waitingList = WaitingList.create("오리", date, theme, reservationTime);
            waitingListRepository.save(waitingList);

            // when
            boolean result = waitingListRepository.existsByNameAndThemeAndDateAndTime("오리", 1L, date, 1L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 대기중이_아니면_false를_반환() {
            // when
            boolean result = waitingListRepository.existsByNameAndThemeAndDateAndTime("오리", 1L, LocalDate.now().plusDays(1), 1L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class findWaitingOrderByIdAndThemeAndDateAndTime {

        @Test
        void 대기가_없으면_0을_반환() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            WaitingList saved = waitingListRepository.save(WaitingList.create("오리", date, theme, reservationTime));
            waitingListRepository.deleteById(saved.getId());

            // when
            int order = waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(saved);

            // then
            assertThat(order).isZero();
        }

        @Test
        void 같은_조건의_대기가_1건이면_대기순서는_1() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            WaitingList saved = waitingListRepository.save(WaitingList.create("오리", date, theme, reservationTime));

            // when
            int order = waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(saved);

            // then
            assertThat(order).isEqualTo(1);
        }

        @Test
        void 먼저_등록된_대기가_있으면_대기순서가_2() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            waitingListRepository.save(WaitingList.create("검프", date, theme, reservationTime));
            WaitingList second = waitingListRepository.save(WaitingList.create("오리", date, theme, reservationTime));

            // when
            int order = waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(second);

            // then
            assertThat(order).isEqualTo(2);
        }
    }
}
