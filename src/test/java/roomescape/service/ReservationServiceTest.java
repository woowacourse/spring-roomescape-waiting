package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.TestFixture;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ReservedThemeResponse;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.ADMIN_NAME;
import static roomescape.TestFixture.DATE_MAY_EIGHTH;
import static roomescape.TestFixture.DATE_MAY_NINTH;
import static roomescape.TestFixture.DATE_MAY_ONE;
import static roomescape.TestFixture.MEMBER_CAT;
import static roomescape.TestFixture.RESERVATION_TIME_SEVEN;
import static roomescape.TestFixture.RESERVATION_TIME_SIX;
import static roomescape.TestFixture.START_AT_SEVEN;
import static roomescape.TestFixture.START_AT_SIX;
import static roomescape.TestFixture.THEME_ANIME_NAME;
import static roomescape.TestFixture.THEME_COMIC;
import static roomescape.TestFixture.THEME_COMIC_NAME;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private ReservationService reservationService;

    private static Stream<LocalDate> invalidLocalDate() {
        return Stream.of(
                LocalDate.now(),
                LocalDate.now().minusDays(1L)
        );
    }

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        // given
        final Reservation reservation = new Reservation(TestFixture.MEMBER_CAT(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), TestFixture.THEME_COMIC());
        given(reservationRepository.save(reservation))
                .willReturn(new Reservation(reservation.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme()));

        // when
        final ReservationResponse response = reservationService.create(reservation);

        // then
        assertThat(response).isNotNull();
    }


    @ParameterizedTest
    @MethodSource("invalidLocalDate")
    @DisplayName("예약 날짜가 현재 날짜 이후가 아닌 경우 예외가 발생한다.")
    void throwExceptionWhenInvalidDate(final LocalDate invalidDate) {
        assertThatThrownBy(() -> reservationService.create(new Reservation(TestFixture.MEMBER_CAT(), invalidDate, RESERVATION_TIME_SIX(), TestFixture.THEME_COMIC())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 초과된 경우 예외가 발생한다.")
    void throwExceptionWhenCreateDuplicatedReservation() {
        // given
        final Theme theme = THEME_COMIC(1L);
        final Reservation reservation = new Reservation(TestFixture.MEMBER_CAT(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), theme);
        given(reservationRepository.existsByDateAndTime_IdAndTheme_Id(DATE_MAY_EIGHTH, RESERVATION_TIME_SIX().getId(), theme.getId()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.create(reservation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAllReservations() {
        // given
        final Reservation reservation1 = new Reservation(TestFixture.MEMBER_CAT(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), TestFixture.THEME_COMIC());
        final Reservation reservation2 = new Reservation(ADMIN(), DATE_MAY_EIGHTH, RESERVATION_TIME_SEVEN(), TestFixture.THEME_ANIME());
        given(reservationRepository.findAll())
                .willReturn(List.of(reservation1, reservation2));

        // when
        final List<ReservationResponse> reservations = reservationService.findAll();

        // then
        assertAll(() -> {
            assertThat(reservations).hasSize(2)
                    .extracting(ReservationResponse::name)
                    .containsExactly(TestFixture.MEMBER_CAT_NAME, ADMIN_NAME);
            assertThat(reservations).extracting(ReservationResponse::date)
                    .containsExactly(DATE_MAY_EIGHTH, DATE_MAY_EIGHTH);
            assertThat(reservations).extracting(ReservationResponse::time)
                    .extracting(ReservationTimeResponse::startAt)
                    .containsExactly(START_AT_SIX, START_AT_SEVEN);
            assertThat(reservations).extracting(ReservationResponse::theme)
                    .extracting(ReservedThemeResponse::name)
                    .containsExactly(THEME_COMIC_NAME, THEME_ANIME_NAME);
        });
    }

    @Test
    @DisplayName("검색 조건에 따른 예약 목록을 조회한다.")
    void findAllByFilterParameter() {
        // given
        final Reservation reservation1 = new Reservation(TestFixture.MEMBER_CAT(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), TestFixture.THEME_COMIC());
        final Reservation reservation2 = new Reservation(TestFixture.MEMBER_CAT(), DATE_MAY_NINTH, RESERVATION_TIME_SIX(), TestFixture.THEME_COMIC());
        final ReservationFilterParam reservationFilterParam = new ReservationFilterParam(
                1L, 1L, LocalDate.parse("2034-05-08"), LocalDate.parse("2034-05-28")
        );
        given(reservationRepository.findByTheme_IdAndMember_IdAndDateBetween(
                1L, 1L, LocalDate.parse("2034-05-08"), LocalDate.parse("2034-05-28"))
        ).willReturn(List.of(reservation1, reservation2));

        // when
        final List<ReservationResponse> reservations
                = reservationService.findAllBy(reservationFilterParam);

        // then
        assertAll(() -> {
            assertThat(reservations).hasSize(2)
                    .extracting(ReservationResponse::name)
                    .containsExactly(TestFixture.MEMBER_CAT_NAME, TestFixture.MEMBER_CAT_NAME);
            assertThat(reservations).extracting(ReservationResponse::time)
                    .extracting(ReservationTimeResponse::startAt)
                    .containsExactly(START_AT_SIX, START_AT_SIX);
            assertThat(reservations).extracting(ReservationResponse::theme)
                    .extracting(ReservedThemeResponse::name)
                    .containsExactly(THEME_COMIC_NAME, THEME_COMIC_NAME);
        });
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        final Long existingId = 1L;
        given(reservationRepository.findById(existingId)).willReturn(
                Optional.of(new Reservation(MEMBER_CAT(1L), DATE_MAY_ONE, RESERVATION_TIME_SEVEN(), THEME_COMIC()))
        );

        // when & then
        assertThatCode(() -> reservationService.delete(existingId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제하려는 예약이 존재하지 않는 경우 예외가 발생한다.")
    void throwExceptionWhenDeleteNotExistingReservation() {
        // given
        final Long notExistingId = 0L;

        // when & then
        assertThatThrownBy(() -> reservationService.delete(notExistingId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
