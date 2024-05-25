package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.repository.dto.ReservationRankResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        final List<Reservation> expected = List.of(
                new Reservation(1L, null, null, null, null),
                new Reservation(2L, null, null, null, null),
                new Reservation(3L, null, null, null, null),
                new Reservation(4L, null, null, null, null),
                new Reservation(5L, null, null, null, null),
                new Reservation(6L, null, null, null, null),
                new Reservation(7L, null, null, null, null),
                new Reservation(8L, null, null, null, null)
        );

        assertThat(reservationRepository.findAll()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회할 경우 빈 값을 반환한다.")
    void findByIdNotPresent() {
        // given
        long id = 100L;

        // when
        Optional<Reservation> actual = reservationRepository.findById(id);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("등록되지 않은 시간 아이디로 예약 존재 여부를 확인한다,")
    void existsByTimeIdNotPresent() {
        // given
        final long timeId = 100L;
        final boolean expected = false;

        // when
        final boolean actual = reservationRepository.existsByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("유명 테마 검색")
    void findPopularTheme() {
        //given
        final LocalDate now = LocalDate.now();
        final List<Theme> expected = List.of(
                new Theme(2L, null, null, null),
                new Theme(1L, null, null, null),
                new Theme(3L, null, null, null)
        );
        //when
        final List<Theme> popularThemes = reservationRepository
                .findPopularThemes(now.minusDays(8), now.minusDays(1), 10);
        //then
        assertThat(popularThemes).isEqualTo(expected);
    }

    @Test
    @DisplayName("조건에 따른 예약 조회")
    void searchReservations() {
        //given
        final LocalDate now = LocalDate.now();
        final ReservationSearchCondition condition = new ReservationSearchCondition(1L, 1L,
                now.minusDays(7), now.minusDays(1));
        final List<Reservation> expected = List.of(new Reservation(1L, null, null, null, null));
        //when
        final List<Reservation> reservations = reservationRepository.findReservationsByCondition(condition.dateFrom(), condition.dateTo(),
                condition.themeId(), condition.memberId());
        //then
        assertThat(reservations).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("자신의 예약 대기 목록 조회")
    @MethodSource("searchMyReservationsParameterProvider")
    void searchMyReservations(final long memberId, final List<ReservationRankResponse> expected) {
        final List<ReservationRankResponse> myReservation = reservationRepository.findMyReservation(memberId);

        assertThat(myReservation).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약 대기만 조회")
    void methodName() {
        final List<Reservation> reservations = reservationRepository.findAllWaiting();
        final List<Reservation> expected = List.of(new Reservation(8L, null, null, null, null));
        assertThat(reservations).isEqualTo(expected);
    }

    static Stream<Arguments> searchMyReservationsParameterProvider() {
        final LocalDate date = LocalDate.now();
        return Stream.of(
                Arguments.of(1L,
                        List.of(
                                new ReservationRankResponse(1L, "봄", date.minusDays(3), LocalTime.of(15, 0), 1),
                                new ReservationRankResponse(3L, "여름", date.minusDays(1), LocalTime.of(16, 0), 1)
                        )),
                Arguments.of(2L,
                        List.of(
                                new ReservationRankResponse(2L, "여름", date.minusDays(2), LocalTime.of(17, 0), 1),
                                new ReservationRankResponse(4L, "여름", date.minusDays(1), LocalTime.of(15, 0), 1),
                                new ReservationRankResponse(7L, "가을", date.plusDays(4), LocalTime.of(18, 0), 1)
                        )),
                Arguments.of(3L,
                        List.of(
                                new ReservationRankResponse(5L, "가을", date.minusDays(7), LocalTime.of(15, 0), 1),
                                new ReservationRankResponse(6L, "가을", date.plusDays(3), LocalTime.of(18, 0), 1),
                                new ReservationRankResponse(8L, "가을", date.plusDays(4), LocalTime.of(18, 0), 2)
                        ))
        );
    }
}
