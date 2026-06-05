package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

@JdbcTest
@Import(ReservationTimeRepository.class)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Nested
    @DisplayName("예약 시간 저장")
    class Save {

        @Test
        void 저장하면_id가_부여된다() {
            ReservationTime time = ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0));

            ReservationTime saved = reservationTimeRepository.save(time);

            assertAll(
                    () -> assertThat(saved.getId()).isNotNull(),
                    () -> assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                    () -> assertThat(saved.getFinishAt()).isEqualTo(LocalTime.of(11, 0))
            );
        }
    }

    @Nested
    @DisplayName("예약 시간 전체 조회")
    class FindAll {

        @Test
        void 저장된_예약_시간을_전체_조회한다() {
            ReservationTime first = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0))
            );
            ReservationTime second = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(12, 0), LocalTime.of(13, 0))
            );

            List<ReservationTime> result = reservationTimeRepository.findAll();

            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result).extracting(ReservationTime::getId)
                            .containsExactly(first.getId(), second.getId()),
                    () -> assertThat(result).extracting(ReservationTime::getStartAt)
                            .containsExactly(LocalTime.of(10, 0), LocalTime.of(12, 0))
            );
        }
    }

    @Nested
    @DisplayName("id로 예약 시간 조회")
    class FindById {

        @Test
        void 존재하는_id면_예약_시간을_반환한다() {
            ReservationTime saved = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0))
            );

            Optional<ReservationTime> result = reservationTimeRepository.findById(saved.getId());

            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                    () -> assertThat(result.get().getFinishAt()).isEqualTo(LocalTime.of(11, 0))
            );
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환한다() {
            Optional<ReservationTime> result = reservationTimeRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("id로 존재 여부 조회")
    class ExistsById {

        @Test
        void 존재하는_id면_true를_반환한다() {
            ReservationTime saved = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0))
            );

            boolean result = reservationTimeRepository.existsById(saved.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_id면_false를_반환한다() {
            boolean result = reservationTimeRepository.existsById(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("시작 시간으로 존재 여부 조회")
    class ExistsByStartAt {

        @Test
        void 존재하는_시작_시간이면_true를_반환한다() {
            reservationTimeRepository.save(ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0)));

            boolean result = reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0));

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_시작_시간이면_false를_반환한다() {
            reservationTimeRepository.save(ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0)));

            boolean result = reservationTimeRepository.existsByStartAt(LocalTime.of(12, 0));

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 예약 시간 조회 (FOR UPDATE)")
    class FindByIdForUpdate {

        @Test
        void 존재하는_id면_예약_시간을_반환한다() {
            ReservationTime saved = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0))
            );

            Optional<ReservationTime> result = reservationTimeRepository.findById(saved.getId());

            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                    () -> assertThat(result.get().getFinishAt()).isEqualTo(LocalTime.of(11, 0))
            );
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환한다() {
            Optional<ReservationTime> result = reservationTimeRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("id로 예약 시간 삭제")
    class DeleteById {

        @Test
        void 삭제하면_해당_예약_시간이_조회되지_않는다() {
            ReservationTime saved = reservationTimeRepository.save(
                    ReservationTime.of(LocalTime.of(10, 0), LocalTime.of(11, 0))
            );

            reservationTimeRepository.deleteById(saved.getId());

            assertThat(reservationTimeRepository.existsById(saved.getId())).isFalse();
        }
    }
}
