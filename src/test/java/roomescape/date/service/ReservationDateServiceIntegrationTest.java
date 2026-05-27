package roomescape.date.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.fixture.ReservationDateFixture;
import roomescape.support.ServiceSupport;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_NOT_FOUND;

@Import(ReservationDateService.class)
class ReservationDateServiceIntegrationTest extends ServiceSupport {

    private static final LocalDate DEFAULT_DATE = LocalDate.of(2099, 1, 1);

    @Autowired
    private ReservationDateService reservationDateService;

    @Test
    @DisplayName("등록된 예약날짜가 여러개이면 조회 시 등록된 개수만큼 반환한다.")
    void readDates() {
        // given
        List<ReservationDate> reservationDates = List.of(
                ReservationDateFixture.oneWeekLater(),
                ReservationDateFixture.twoWeeksLater()
        );
        saveAll(reservationDates);

        // when
        List<ReservationDate> actual = reservationDateService.readDates();

        // then
        Assertions.assertThat(actual)
                .hasSize(reservationDates.size());
    }

    @Test
    @DisplayName("등록된 예약날짜와 조회된 예약날짜의 모든 필드는 일치한다")
    void readDate() {
        // given
        ReservationDate saved = saveDate(ReservationDateFixture.oneWeekLater());

        // when
        ReservationDate actual = reservationDateService.readDate(saved.getId());

        // then
        Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(saved);
    }

    @Test
    @DisplayName("등록되지 않은 예약날짜를 조회하면 예외가 발생한다.")
    void readDate_deregistered() {
        // given
        Long deregisteredId = Long.MIN_VALUE;

        // when & then
        Assertions.assertThatThrownBy(() -> reservationDateService.readDate(deregisteredId))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예약날짜를 1개 등록하면 예약날짜 데이터 수가 1 증가한다.")
    void register() {
        // given
        List<ReservationDate> emptyDates = List.of();

        // when
        reservationDateService.register(DEFAULT_DATE);

        // then
        Assertions.assertThat(reservationDateRepository.findAll())
                .hasSize(emptyDates.size() + 1);
    }

    @Test
    @DisplayName("등록한 예약날짜와 다시 조회한 예약날짜의 모든 필드가 일치한다.")
    void register_theme_fields_match() {
        // when
        ReservationDate registered = reservationDateService.register(DEFAULT_DATE);

        // then
        assertThat(registered)
                .usingRecursiveComparison()
                .isEqualTo(reservationDateRepository.findById(registered.getId()).get());
    }

    @Test
    @DisplayName("등록되지않은 예약날짜의 상태를 변경하면 예외가 발생한다.")
    void updateStatus_deregistered() {
        // given
        Long deregisteredId = Long.MIN_VALUE;

        // when  & then
        Assertions.assertThatThrownBy(() -> reservationDateService.updateStatus(deregisteredId, false))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 등록된 날짜를 또 등록하면 예외가 발생한다.")
    void existsByDate_duplicated_date() {
        // given
        ReservationDate date = saveDate(ReservationDateFixture.oneWeekLater());
        LocalDate duplicatedDate = date.getDate();

        // when & then
        Assertions.assertThatThrownBy(() -> reservationDateService.register(duplicatedDate))
                .isInstanceOf(ReservationDateException.class)
                .hasMessage(DATE_ALREADY_EXISTS.getMessage());
    }


    private List<ReservationDate> saveAll(List<ReservationDate> dates) {
        List<ReservationDate> saved = new ArrayList<>();
        for (ReservationDate reservationDate : dates) {
            saved.add(saveDate(reservationDate));
        }
        return saved;
    }

}
