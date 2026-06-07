package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import(ReservationTimeRepository.class)
@Sql(scripts = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationTimeRepositoryTest {

    @Autowired ReservationTimeRepository reservationTimeRepository;

    @Nested
    class save {

        @Test
        void 저장_후_id가_부여된_객체를_반환() {
            // given
            ReservationTime time = ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0));

            // when
            ReservationTime saved = reservationTimeRepository.save(time);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
            assertThat(saved.getEndAt()).isEqualTo(LocalTime.of(11, 0));
        }
    }

    @Nested
    class findAll {

        @Test
        void 저장된_모든_시간을_반환() {
            // given
            reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0)));
            reservationTimeRepository.save(ReservationTime.create(LocalTime.of(14, 0), LocalTime.of(15, 0)));

            // when
            List<ReservationTime> result = reservationTimeRepository.findAll();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        void 저장된_시간이_없으면_빈_목록을_반환() {
            // when
            List<ReservationTime> result = reservationTimeRepository.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findById {

        @Test
        void 존재하는_id면_시간을_반환() {
            // given
            ReservationTime saved = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0)));

            // when
            Optional<ReservationTime> result = reservationTimeRepository.findById(saved.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환() {
            // when
            Optional<ReservationTime> result = reservationTimeRepository.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class delete {

        @Test
        void 존재하는_id면_삭제_후_true를_반환() {
            // given
            ReservationTime saved = reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0)));

            // when
            boolean result = reservationTimeRepository.delete(saved.getId());

            // then
            assertThat(result).isTrue();
            assertThat(reservationTimeRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_id면_false를_반환() {
            // when
            boolean result = reservationTimeRepository.delete(999L);

            // then
            assertThat(result).isFalse();
        }
    }
}
