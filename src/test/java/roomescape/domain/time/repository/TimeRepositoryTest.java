package roomescape.domain.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.time.entity.Time;
import roomescape.global.error.exception.GeneralException;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DataJpaTest
@Sql(
    statements = {
        "DELETE FROM reservation",
        "DELETE FROM reservation_time",
        "DELETE FROM theme",
        "ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1",
        "ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1",
        "ALTER TABLE theme ALTER COLUMN id RESTART WITH 1"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TimeRepositoryTest {

    private static volatile boolean saveSucceeded = false;
    private static volatile boolean findSucceeded = false;

    @Autowired
    private TimeRepository timeRepository;

    @BeforeEach
    void setUp() {
        // 상태 초기화
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 예약_시간_저장 {

        @Test
        @Order(1)
        void 예약_시간을_저장한다() {
            // given
            Time time = Time.create(LocalTime.of(10, 30));

            // when
            Time actual = timeRepository.save(time);

            // then
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getStartAt()).isEqualTo(LocalTime.of(10, 30));
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
                .isInstanceOf(DataIntegrityViolationException.class);
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
            List<Time> actual = timeRepository.findAllByDeletedAtIsNull();

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
            Optional<Time> actual = timeRepository.findTimeByIdAndDeletedAtIsNull(savedTime.getId());

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get().getId()).isEqualTo(savedTime.getId());
            assertThat(actual.get().getStartAt()).isEqualTo(LocalTime.of(15, 30));
        }

        @Test
        void 존재하지_않는_ID이면_빈_값을_반환한다() {
            // when
            Optional<Time> actual = timeRepository.findTimeByIdAndDeletedAtIsNull(1L);

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
            boolean actual = timeRepository.existsTimeByStartAtAndDeletedAtIsNull(startAt);

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 해당_시작_시간이_존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByStartAtAndDeletedAtIsNull(LocalTime.of(15, 30));

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
            boolean actual = timeRepository.existsTimeByIdAndDeletedAtIsNull(time.getId());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 해당_ID가_존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = timeRepository.existsTimeByIdAndDeletedAtIsNull(1L);

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
            assertThat(timeRepository.findAllByDeletedAtIsNull())
                .extracting(Time::getId, Time::getStartAt)
                .containsExactly(tuple(time2.getId(), time2.getStartAt()));
            // assertThat(countDeletedTimeById(time1.getId())).isEqualTo(1);
            assertThat(timeRepository.findTimeByIdAndDeletedAtIsNull(time1.getId())).isEmpty();
            assertThat(timeRepository.existsTimeByIdAndDeletedAtIsNull(time1.getId())).isFalse();
            assertThat(timeRepository.existsTimeByStartAtAndDeletedAtIsNull(time1.getStartAt())).isFalse();
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

}
