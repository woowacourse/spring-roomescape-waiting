package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.util.DateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationConditionRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.JpaThemeRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private JpaReservationRepository jpaReservationRepository;
    @Mock
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Mock
    private JpaThemeRepository jpaThemeRepository;
    @Mock
    private JpaMemberRepository jpaMemberRepository;
    @Mock
    private DateTime dateTime;

    private static Stream<Arguments> getConditionalReservations_test() {
        return Stream.of(
                Arguments.of(1L, null, null, null),
                Arguments.of(2L, null, null, null),
                Arguments.of(null, 1L, null, null),
                Arguments.of(null, 2L, null, null),
                Arguments.of(null, 3L, null, null),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), null),
                Arguments.of(null, null, LocalDate.of(2024, 10, 7), null),
                Arguments.of(null, null, LocalDate.of(2024, 10, 8), null),
                Arguments.of(null, null, LocalDate.of(2024, 10, 9), null),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 8)),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 7)),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 6)),
                Arguments.of(null, null, null, LocalDate.of(2024, 10, 5)),
                Arguments.of(null, null, LocalDate.of(2024, 10, 6), LocalDate.of(2024, 10, 7))
        );
    }

    @DisplayName("중복 예약이 불가하다.")
    @Test
    void cant_not_reserve_duplicate() {
        // given
        when(jpaReservationTimeRepository.findById(1L))
                .thenReturn(
                        Optional.of(ReservationTime.createWithId(1L, LocalTime.of(10, 0)))
                );
        when(jpaThemeRepository.findById(1L)).thenReturn(Optional.of(
                Theme.createWithId(1L, "테마", "설명", "썸넬")
        ));
        when(jpaMemberRepository.findById(1L)).thenReturn(Optional.of(
                Member.createWithId(1L, "멤버", "a", "1234", Role.USER)
        ));
        when(dateTime.now()).thenReturn(LocalDateTime.of(2024, 9, 6, 19, 23));
        when(jpaReservationRepository.existsByDateAndTime_StartAtAndTheme_Id(any(LocalDate.class), any(LocalTime.class),
                eq(1L)))
                .thenReturn(true);
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(
                new ReservationRequest(LocalDate.of(2024, 10, 6), 1L, 1L), 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제 명령을 1번 요청한다.")
    void deleteReservationById_test() {
        // when
        reservationService.deleteReservationById(1L);
        // then
        verify(jpaReservationRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("조건이 없을 때는 모든 예약을 들고 온다.")
    void getAllReservations_test() {
        // given
        List<Reservation> reservations = createReservations();

        ReservationResponse expected1 = ReservationResponse.from(reservations.get(0));
        ReservationResponse expected2 = ReservationResponse.from(reservations.get(1));
        ReservationResponse expected3 = ReservationResponse.from(reservations.get(2));

        when(jpaReservationRepository.findAll()).thenReturn(reservations);
        ReservationConditionRequest request = new ReservationConditionRequest(null, null, null, null);

        // when
        List<ReservationResponse> responses = reservationService.getReservations(request);

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0)).isEqualTo(expected1);
        assertThat(responses.get(1)).isEqualTo(expected2);
        assertThat(responses.get(2)).isEqualTo(expected3);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("조건이 있을 경우 조건에 맞는 예약을 들고 온다.")
    void getConditionalReservations_test(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        // given
        List<Reservation> reservations = createReservations();

        ReservationResponse expected1 = ReservationResponse.from(reservations.get(0));
        ReservationResponse expected2 = ReservationResponse.from(reservations.get(1));
        ReservationResponse expected3 = ReservationResponse.from(reservations.get(2));

        when(jpaReservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, dateFrom, dateTo)).thenReturn(
                reservations);
        ReservationConditionRequest request = new ReservationConditionRequest(memberId, themeId, dateFrom, dateTo);

        // when
        List<ReservationResponse> responses = reservationService.getReservations(request);
        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0)).isEqualTo(expected1);
        assertThat(responses.get(1)).isEqualTo(expected2);
        assertThat(responses.get(2)).isEqualTo(expected3);
    }

    @Test
    @DisplayName("본인 예약들을 dto로 변환한다.")
    void getMyReservations_dto_test() {
        // given
        List<Reservation> reservations = createReservations();
        when(jpaReservationRepository.findByMember_Id(1L))
                .thenReturn(reservations);
        MyReservationResponse expected1 = MyReservationResponse.from(reservations.get(0));
        MyReservationResponse expected2 = MyReservationResponse.from(reservations.get(1));
        MyReservationResponse expected3 = MyReservationResponse.from(reservations.get(2));

        // when
        List<MyReservationResponse> responses = reservationService.getMyReservations(1L);
        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0)).isEqualTo(expected1);
        assertThat(responses.get(1)).isEqualTo(expected2);
        assertThat(responses.get(2)).isEqualTo(expected3);
    }

    private List<Reservation> createReservations() {
        Theme theme1 = Theme.createWithId(1L, "테스트1", "설명", "localhost:8080");
        Theme theme2 = Theme.createWithId(2L, "테스트2", "설명", "localhost:8080");

        ReservationTime reservationTime1 = ReservationTime.createWithoutId(LocalTime.of(10, 0));

        Member member = Member.createWithId(1L, "홍길동", "a", "a", Role.USER);

        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2024, 10, 6),
                reservationTime1, theme1);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2024, 10, 7),
                reservationTime1, theme2);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2024, 10, 8),
                reservationTime1, theme2);

        return List.of(reservation1, reservation2, reservation3);
    }
}
