package roomescape.feature.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.feature.time.domain.Time;
import roomescape.global.domain.EntityStatus;
import roomescape.global.error.exception.GeneralException;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class JdbcTimeRepositoryTest {

    private static volatile boolean saveSucceeded = false;
    private static volatile boolean findSucceeded = false;

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
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 예약_시간_저장 {

        @Test
        @Order(1)
        void 예약_시간을_저장한다() {
            // given
            LocalTime startAt = LocalTime.of(10, 30);
            Time time = Time.create(startAt);

            // when
            Long savedTimeId = timeRepository.save(time).getId();

            Time expectedSavedTime = Time.reconstruct(savedTimeId, startAt, EntityStatus.ACTIVE);

            // then
            Time actualSavedTime = jdbcTemplate.queryForObject(
                "SELECT id, start_at, status FROM reservation_time WHERE id = ?",
                (rs, rowNum) -> Time.reconstruct(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime(),
                    EntityStatus.valueOf(rs.getString("status"))
                ),
                savedTimeId
            );

            assertThat(actualSavedTime).usingRecursiveComparison()
                .isEqualTo(expectedSavedTime);
            saveSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제되지_않은_같은_예약_시간은_중복_저장할_수_없다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            LocalTime startAt = LocalTime.of(10, 30);
            timeRepository.save(Time.create(startAt));

            // when & then
            assertThatThrownBy(() -> timeRepository.save(Time.create(startAt)))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 예약_시간_목록_조회 {

        @BeforeEach
        void assumeSaveWorks() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        @Order(1)
        void 활성_예약_시간을_조회한다() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 30)));
            Time time3 = timeRepository.save(Time.create(LocalTime.of(13, 0)));

            // when
            List<Time> actual = timeRepository.findAllByNotDeleted();

            // then
            assertThat(actual)
                .extracting(Time::getId, Time::getStartAt)
                .containsExactly(
                    tuple(time1.getId(), time1.getStartAt()),
                    tuple(time2.getId(), time2.getStartAt()),
                    tuple(time3.getId(), time3.getStartAt())
                );
            findSucceeded = true;
        }
    }

    @Nested
    class 예약_시간_ID_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약_시간을_ID로_조회한다() {
            // given
            Time savedTime = timeRepository.save(Time.create(LocalTime.of(15, 30)));

            // when
            Optional<Time> actual = timeRepository.findTimeByIdAndNotDeleted(savedTime.getId());

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get().getId()).isEqualTo(savedTime.getId());
            assertThat(actual.get().getStartAt()).isEqualTo(LocalTime.of(15, 30));
        }

        @Test
        void 존재하지_않는_ID이면_빈_값을_반환한다() {
            // when
            Optional<Time> actual = timeRepository.findTimeByIdAndNotDeleted(1L);

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class 예약_시간_시작_시간_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 해당_시작_시간이_존재하면_true를_반환한다() {
            // given
            LocalTime startAt = LocalTime.of(15, 30);
            timeRepository.save(Time.create(startAt));

            // when
            boolean actual = timeRepository.existsTimeByStartAtAndNotDeleted(startAt);

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 해당_시작_시간이_존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByStartAtAndNotDeleted(LocalTime.of(15, 30));

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class 예약_시간_ID_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 해당_ID가_존재하면_true를_반환한다() {
            // given
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));

            // when
            boolean actual = timeRepository.existsTimeByIdAndNotDeleted(time.getId());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 해당_ID가_존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByIdAndNotDeleted(1L);

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class 예약_시간_삭제 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약_시간을_소프트_삭제한다() {
            // given
            Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
            Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));

            // when
            timeRepository.deleteTimeById(time1.getId());

            // then
            assertThat(timeRepository.findAllByNotDeleted())
                .extracting(Time::getId, Time::getStartAt)
                .containsExactly(tuple(time2.getId(), time2.getStartAt()));
            assertThat(countDeletedTimeById(time1.getId())).isEqualTo(1);
            assertThat(timeRepository.findTimeByIdAndNotDeleted(time1.getId())).isEmpty();
            assertThat(timeRepository.existsTimeByIdAndNotDeleted(time1.getId())).isFalse();
            assertThat(timeRepository.existsTimeByStartAtAndNotDeleted(time1.getStartAt())).isFalse();
        }

        @Test
        void 이미_삭제된_시간을_삭제하면_예외가_발생한다() {
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
            "SELECT COUNT(*) FROM reservation_time WHERE id = ? AND status = 'DELETED'",
            Integer.class,
            id
        );
    }
}
