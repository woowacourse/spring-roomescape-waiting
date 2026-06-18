package roomescape.date.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.date.repository.ReservationDateRepository;

@DataJpaTest(showSql = false)
class ReservationDateServiceTest {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 1, 1);

    @Autowired
    private ReservationDateRepository reservationDateRepository;
    private ReservationDateService reservationDateService;

    @BeforeEach
    void setUp() {
        reservationDateService = new ReservationDateService(reservationDateRepository);
    }

    private List<ReservationDate> saveAll(List<ReservationDate> dates) {
        List<ReservationDate> saved = new ArrayList<>();
        for (ReservationDate reservationDate : dates) {
            saved.add(reservationDateRepository.save(reservationDate));
        }
        return saved;
    }


    @Nested
    @DisplayName("readDates 메서드는")
    class ReadDatesTest {


        @Test
        @DisplayName("모든 날짜를 가져온다")
        void 성공() {
            // given
            List<ReservationDate> reservationDates = List.of(
                ReservationDateFixture.oneWeekLater(),
                ReservationDateFixture.twoWeeksLater()
            );
            saveAll(reservationDates);

            // when
            List<ReservationDate> actual = reservationDateService.readDates();

            // then
            assertThat(actual)
                .hasSize(reservationDates.size());
        }
    }

    @Nested
    @DisplayName("readDate 메서드는")
    class ReadDateTest {


        @Test
        @DisplayName("한 날짜를 가져온다")
        void 성공() {
            // given
            ReservationDate saved = reservationDateRepository.save(
                ReservationDateFixture.oneWeekLater());

            // when
            ReservationDate actual = reservationDateService.readDate(saved.getId());

            // then
            assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
        }


        @Test
        @DisplayName("등록되지 않은 날짜를 가져오려고 하면 예외가 발생한다")
        void 실패() {
            // given
            Long deregisteredId = Long.MIN_VALUE;

            // when & then
            assertThatThrownBy(() -> reservationDateService.readDate(deregisteredId))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("register 메서드는")
    class RegisterTest {


        @Test
        @DisplayName("날짜를 등록한다")
        void 성공1() {
            // given
            List<ReservationDate> emptyDates = List.of();

            // when
            reservationDateService.register(DEFAULT_DATE);

            // then
            assertThat(reservationDateRepository.findAll())
                .hasSize(emptyDates.size() + 1);
        }


        @Test
        @DisplayName("등록한 날짜와 조회된 날짜가 일치해야 한다")
        void 성공2() {
            // when
            ReservationDate registered = reservationDateService.register(DEFAULT_DATE);

            // then
            assertThat(registered)
                .usingRecursiveComparison()
                .isEqualTo(reservationDateRepository.findById(registered.getId()).get());
        }


        @Test
        @DisplayName("이미 등록된 날짜이면 예외가 발생한다")
        void 실패() {
            // given
            ReservationDate date = ReservationDateFixture.oneWeekLater();
            reservationDateRepository.save(date);
            LocalDate duplicatedDate = date.getDate();

            // when & then
            assertThatThrownBy(() -> reservationDateService.register(duplicatedDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("등록되지 않은 날짜의 상태를 변경하려고 하면 예외가 발생한다")
        void 실패() {
            // given
            Long deregisteredId = Long.MIN_VALUE;

            // when  & then
            assertThatThrownBy(
                () -> reservationDateService.updateStatus(deregisteredId, false))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
        }
    }
}
