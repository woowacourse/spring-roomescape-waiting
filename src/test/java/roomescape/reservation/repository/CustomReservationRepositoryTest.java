package roomescape.reservation.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.model.Reservation;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CustomReservationRepositoryTest {

    @Autowired
    private CustomReservationRepository customReservationRepository;

    @DisplayName("테마 id, 시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithThemeIdAndFromAndToConditionTest() {
        // Given
        final long themeId = 1L;
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, themeId, from, to);

        // When
        final List<Reservation> reservations = customReservationRepository.searchReservations(searchReservationsParams);

        // Then
        assertThat(reservations).hasSize(4);
    }

    @DisplayName("시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithFromAndToConditionTest() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, null, from, to);

        // When
        final List<Reservation> reservations = customReservationRepository.searchReservations(searchReservationsParams);

        // Then
        assertThat(reservations).hasSize(12);
    }

    @DisplayName("시작일 이후의 예약 정보를 조회한다.")
    @Test
    void searchReservationsAfterFromConditionTest() {
        // Given
        final LocalDate from = LocalDate.now().minusDays(7);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(null, null, from, null);

        // When
        final List<Reservation> reservations = customReservationRepository.searchReservations(searchReservationsParams);

        // Then
        assertThat(reservations).hasSize(16);
    }

    @DisplayName("회원 id, 테마 id, 시작일, 종료일을 기준으로 예약 정보를 조회한다.")
    @Test
    void searchReservationsWithAllConditionTest() {
        // Given
        final long memberId = 1L;
        final long themeId = 11L;
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().plusDays(1);
        final SearchReservationsParams searchReservationsParams = new SearchReservationsParams(memberId, themeId, from, to);

        // When
        final List<Reservation> reservations = customReservationRepository.searchReservations(searchReservationsParams);

        // Then
        assertThat(reservations).hasSize(1);
    }
}
