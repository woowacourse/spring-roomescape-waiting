package roomescape.date.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.date.domain.ReservationDate;
import roomescape.date.fixture.ReservationDateFixture;

@DataJpaTest(showSql = false)
class ReservationDateRepositoryTest {

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    private List<ReservationDate> saveAll(List<ReservationDate> reservationDates) {
        List<ReservationDate> savedReservationDates = new ArrayList<>();
        for (ReservationDate reservationDate : reservationDates) {
            savedReservationDates.add(save(reservationDate));
        }
        return savedReservationDates;
    }

    private ReservationDate save(ReservationDate reservationDate) {
        return reservationDateRepository.save(reservationDate);
    }


    @Nested
    @DisplayName("findAll 메서드는")
    class FindAllTest {


        @Test
        @DisplayName("모든 날짜를 조회한다")
        void 성공() {
            // given
            List<ReservationDate> reservationDates = List.of(
                ReservationDateFixture.oneWeekLater(),
                ReservationDateFixture.twoWeeksLater()
            );
            saveAll(reservationDates);

            // when
            List<ReservationDate> actual = reservationDateRepository.findAll();

            // then
            assertThat(actual)
                .hasSize(reservationDates.size());
        }
    }

    @Nested
    @DisplayName("findById 메서드는")
    class FindByIdTest {


        @Test
        @DisplayName("요청한 id를 가진 날짜를 조회한다")
        void 성공() {
            // given
            ReservationDate saved = save(ReservationDateFixture.oneWeekLater());

            // when
            ReservationDate actual = reservationDateRepository.findById(saved.getId()).get();

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
        }


        @Test
        @DisplayName("요청한 id가 존재하지 않으면 optional.empty를 반환한다")
        void 실패() {
            // given
            Long wrongId = Long.MIN_VALUE;

            // when
            Optional<ReservationDate> actual = reservationDateRepository.findById(wrongId);

            // then
            assertThat(actual)
                .isEmpty();
        }
    }

    @Nested
    @DisplayName("save 메서드는")
    class SaveTest {


        @Test
        @DisplayName("날짜를 생성한다")
        void 성공() {
            // given
            List<ReservationDate> reservationDates = List.of();

            // when
            reservationDateRepository.save(ReservationDateFixture.oneWeekLater());

            // then
            assertThat(reservationDateRepository.findAll())
                .hasSize(reservationDates.size() + 1);
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("status를 변경한다 - true")
        void 성공1() {
            // given
            ReservationDate saved = save(ReservationDateFixture.oneWeekLater());
            saved.updateStatus(true);

            // when
            reservationDateRepository.saveAndFlush(saved);

            // then
            assertThat(reservationDateRepository.findById(saved.getId()).get().isActive())
                .isTrue();
        }


        @Test
        @DisplayName("status를 변경한다 - false")
        void 성공2() {
            // given
            ReservationDate saved = save(ReservationDateFixture.activeOneWeekLater());
            saved.updateStatus(false);

            // when
            reservationDateRepository.saveAndFlush(saved);

            // then
            assertThat(
                reservationDateRepository.findById(saved.getId()).get().isActive())
                .isFalse();
        }
    }

    @Nested
    @DisplayName("existsByDate 메서드는")
    class ExistsByDateTest {


        @Test
        @DisplayName("요청한 날짜가 존재하는지 확인한다")
        void 성공() {
            // given
            ReservationDate saved = save(ReservationDateFixture.activeOneWeekLater());

            // when
            boolean result = reservationDateRepository.existsByDate(saved.getDate());

            // then
            assertThat(result)
                .isTrue();
        }
    }
}
