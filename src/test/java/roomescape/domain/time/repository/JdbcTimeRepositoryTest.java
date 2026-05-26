package roomescape.domain.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.exception.GeneralException;

class JdbcTimeRepositoryTest {

    private JdbcTimeRepository timeRepository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        timeRepository = new JdbcTimeRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    class SaveTest {

        @Test
        void 성공() {
            // given
            Time time = Time.create(LocalTime.of(10, 30));

            // when
            Time actual = timeRepository.save(time);

            // then
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(10, 30));
        }

        @Test
        void 삭제되지_않은_같은_예약_시간은_중복_저장할_수_없다() {
            // given
            LocalTime startAt = LocalTime.of(10, 30);
            timeRepository.save(Time.create(startAt));

            // when & then
            assertThatThrownBy(() -> timeRepository.save(Time.create(startAt)))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Nested
    class FindAllTimesTest {

        @Test
        void 성공() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 30)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(13, 0)));

            // when
            List<Time> actual = timeRepository.findAllByDeletedAtIsNull();

            // then
            assertThat(actual)
                .extracting(Time::getId, Time::getStartAt)
                .containsExactly(
                    tuple(time1.getId(), time1.getStartAt()),
                    tuple(time2.getId(), time2.getStartAt()),
                    tuple(time3.getId(), time3.getStartAt())
                );
        }
    }

    @Nested
    class FindTimeByIdTest {

        @Test
        void 성공() {
            // given
            Time savedTime = timeRepository.save(Time.create(LocalTime.of(15, 30)));

            // when
            Optional<Time> actual = timeRepository.findTimeByIdAndDeletedAtIsNull(savedTime.getId());

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get().getId()).isEqualTo(savedTime.getId());
            assertThat(actual.get().getStartAt()).isEqualTo(LocalTime.of(15, 30));
        }

        @Test
        void 존재하지_않으면_빈_값을_반환한다() {
            // when
            Optional<Time> actual = timeRepository.findTimeByIdAndDeletedAtIsNull(1L);

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class ExistsTimeByStartAtTest {

        @Test
        void 존재하면_true를_반환한다() {
            // given
            LocalTime startAt = LocalTime.of(15, 30);
            timeRepository.save(Time.create(startAt));

            // when
            boolean actual = timeRepository.existsTimeByStartAtAndDeletedAtIsNull(startAt);

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByStartAtAndDeletedAtIsNull(LocalTime.of(15, 30));

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class ExistsTimeByIdTest {

        @Test
        void 존재하면_true를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));

            // when
            boolean actual = timeRepository.existsTimeByIdAndDeletedAtIsNull(time.getId());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByIdAndDeletedAtIsNull(1L);

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class DeleteTimeByIdTest {

        @Test
        void 성공() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));

            // when
            timeRepository.deleteTimeById(time1.getId());

            // then
            assertThat(timeRepository.findAllByDeletedAtIsNull())
                .extracting(Time::getId, Time::getStartAt)
                .containsExactly(tuple(time2.getId(), time2.getStartAt()));
            assertThat(countDeletedTimeById(time1.getId())).isEqualTo(1);
            assertThat(timeRepository.findTimeByIdAndDeletedAtIsNull(time1.getId())).isEmpty();
            assertThat(timeRepository.existsTimeByIdAndDeletedAtIsNull(time1.getId())).isFalse();
            assertThat(timeRepository.existsTimeByStartAtAndDeletedAtIsNull(time1.getStartAt())).isFalse();
        }

        @Test
        void 삭제하려는_시간이_이미_삭제되었으면_예외가_발생한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            timeRepository.deleteTimeById(time.getId());

            // when & then
            assertThatThrownBy(() -> timeRepository.deleteTimeById(time.getId()))
                .isInstanceOf(GeneralException.class)
                .hasMessage("예약 시간을 찾을 수 없습니다.");
        }
    }

    private Integer countDeletedTimeById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservation_time WHERE id = ? AND deleted_at IS NOT NULL",
            Integer.class,
            id
        );
    }
}
