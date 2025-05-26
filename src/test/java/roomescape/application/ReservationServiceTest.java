package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.BusinessRuleViolationException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService service;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("예약을 추가할 수 있다.")
    void saveReservation() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);

        // when
        Reservation reserved = service.saveReservation(2L, tomorrow, 2L, 2L);

        // then
        var reservations = reservationRepository.findAll();
        assertThat(reservations).contains(reserved);
    }

    @Test
    @DisplayName("미래의 날짜와 시간에 대한 예약 생성은 가능하다.")
    void saveReservation_WhenReserveFuture() {
        // given
        var tomorrow = LocalDate.now().plusDays(1);

        // when & then
        assertThatCode(() -> service.saveReservation(2L, tomorrow, 2L, 2L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("과거의 날짜와 시간에 대한 예약 생성 시 예외를 던진다.")
    void saveReservation_WhenReservePast() {
        // given
        var yesterday = LocalDate.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> service.saveReservation(2L, yesterday, 2L, 2L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이전 날짜로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 예약된 날짜와 시간에 대한 예약 생성 시 예외를 던진다.")
    void saveReservation_WhenReservationAlreadyExists() {
        // given
        var reservedUserId = 2L;
        var reservedDate = LocalDate.parse("2025-05-05");
        var reservedThemeId = 1L;
        var reservedTimeSlotId = 1L;

        // when & then
        assertThatThrownBy(
                () -> service.saveReservation(reservedUserId, reservedDate, reservedTimeSlotId, reservedThemeId))
                .isInstanceOf(AlreadyExistedException.class)
                .hasMessage("이미 예약된 날짜, 시간, 테마에 대한 예약은 불가능합니다.");
    }

    @Test
    @DisplayName("검색 필터로 예약을 조회할 수 있다.")
    void findReservationsByFilter() {
        // given
        var filter =
                new ReservationSearchFilter(1L, 2L, LocalDate.parse("2025-05-05"), LocalDate.parse("2025-05-06"));

        // when & then
        assertThat(service.findReservationsByFilter(filter)).hasSize(2);
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다.")
    void removeById() {
        // when
        service.removeById(1L);

        // then
        var reservations = reservationRepository.findAll();
        assertThat(reservations.getFirst().id()).isEqualTo(2L);
    }
}
