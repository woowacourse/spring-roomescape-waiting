package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static roomescape.TestFixture.*;
import static roomescape.TestFixture.RESERVATION_TIME_SIX;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.TestFixture;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.theme.Theme;
import roomescape.dto.reservation.ReservationExistenceCheck;
import roomescape.dto.reservation.ReservationFilterParam;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.ReservationTimeResponse;
import roomescape.dto.theme.ReservedThemeResponse;

import java.time.LocalDate;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationDao reservationDao;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        // given
        final Reservation reservation = new Reservation(TestFixture.MEMBER_MIA(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), THEME_HORROR());
        given(reservationDao.save(reservation))
                .willReturn(new Reservation(1L, reservation.getMember(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme()));

        // when
        final ReservationResponse response = reservationService.create(reservation);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 예약이 초과된 경우 예외가 발생한다.")
    void throwExceptionWhenCreateDuplicatedReservation() {
        // given
        final Theme theme = THEME_HORROR(1L);
        final Reservation reservation = new Reservation(TestFixture.MEMBER_MIA(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), theme);
        final ReservationExistenceCheck reservationExistenceCheck
                = new ReservationExistenceCheck(LocalDate.parse(DATE_MAY_EIGHTH), RESERVATION_TIME_SIX().getId(), theme.getId());
        given(reservationDao.findAllBy(reservationExistenceCheck))
                .willReturn(List.of(new Reservation(1L, reservation.getMember(), reservation.getDate(),
                        reservation.getTime(), reservation.getTheme())));

        // when & then
        assertThatThrownBy(() -> reservationService.create(reservation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAllReservations() {
        // given
        final Reservation reservation1 = new Reservation(TestFixture.MEMBER_MIA(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), THEME_HORROR());
        final Reservation reservation2 = new Reservation(ADMIN(), DATE_MAY_EIGHTH, RESERVATION_TIME_SEVEN(), THEME_DETECTIVE());
        given(reservationDao.findAll())
                .willReturn(List.of(reservation1, reservation2));

        // when
        final List<ReservationResponse> reservations = reservationService.findAll();

        // then
        assertAll(() -> {
            assertThat(reservations).hasSize(2)
                    .extracting(ReservationResponse::name)
                    .containsExactly(TestFixture.MEMBER_MIA_NAME, ADMIN_NAME);
            assertThat(reservations).extracting(ReservationResponse::date)
                    .containsExactly(LocalDate.parse(DATE_MAY_EIGHTH), LocalDate.parse(DATE_MAY_EIGHTH));
            assertThat(reservations).extracting(ReservationResponse::time)
                    .extracting(ReservationTimeResponse::startAt)
                    .containsExactly(START_AT_SIX, START_AT_SEVEN);
            assertThat(reservations).extracting(ReservationResponse::theme)
                    .extracting(ReservedThemeResponse::name)
                    .containsExactly(THEME_HORROR_NAME, THEME_DETECTIVE_NAME);
        });
    }

    @Test
    @DisplayName("검색 조건에 따른 예약 목록을 조회한다.")
    void findAllByFilterParameter() {
        // given
        final Reservation reservation1 = new Reservation(TestFixture.MEMBER_MIA(), DATE_MAY_EIGHTH, RESERVATION_TIME_SIX(), THEME_HORROR());
        final Reservation reservation2 = new Reservation(TestFixture.MEMBER_MIA(), DATE_MAY_NINTH, RESERVATION_TIME_SIX(), THEME_HORROR());
        final ReservationFilterParam reservationFilterParam
                = new ReservationFilterParam(1L, 1L,
                LocalDate.parse("2034-05-08"), LocalDate.parse("2034-05-28"));
        given(reservationDao.findAllBy(reservationFilterParam))
                .willReturn(List.of(reservation1, reservation2));

        // when
        final List<ReservationResponse> reservations
                = reservationService.findAllBy(reservationFilterParam);

        // then
        assertAll(() -> {
            assertThat(reservations).hasSize(2)
                    .extracting(ReservationResponse::name)
                    .containsExactly(TestFixture.MEMBER_MIA_NAME, TestFixture.MEMBER_MIA_NAME);
            assertThat(reservations).extracting(ReservationResponse::time)
                    .extracting(ReservationTimeResponse::startAt)
                    .containsExactly(START_AT_SIX, START_AT_SIX);
            assertThat(reservations).extracting(ReservationResponse::theme)
                    .extracting(ReservedThemeResponse::name)
                    .containsExactly(THEME_HORROR_NAME, THEME_HORROR_NAME);
        });
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        final Long existingId = 1L;
        given(reservationDao.existById(existingId)).willReturn(true);

        // when & then
        assertThatCode(() -> reservationService.delete(existingId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삭제하려는 예약이 존재하지 않는 경우 예외가 발생한다.")
    void throwExceptionWhenDeleteNotExistingReservation() {
        // given
        final Long notExistingId = 1L;
        given(reservationDao.existById(notExistingId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reservationService.delete(notExistingId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
