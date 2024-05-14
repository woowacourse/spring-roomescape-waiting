package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.Fixture;
import roomescape.domain.LoginMember;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.CollectionMemberRepository;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;
import roomescape.repository.CollectionThemeRepository;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

class ReservationServiceTest {

    private final LoginMember loginMember = Fixture.defaultLoginuser;
    private ReservationRepository reservationRepository;
    private ReservationService reservationService;
    private MemberRepository memberRepository;
    private ReservationTime defaultTime = new ReservationTime(LocalTime.now());
    private Theme defaultTheme = new Theme("name", "description", "thumbnail");

    @BeforeEach
    void initService() {
        CollectionReservationTimeRepository reservationTimeRepository = new CollectionReservationTimeRepository();
        ThemeRepository themeRepository = new CollectionThemeRepository();
        reservationRepository = new CollectionReservationRepository();
        memberRepository = new CollectionMemberRepository();
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository);
        defaultTime = reservationTimeRepository.save(defaultTime);
        defaultTheme = themeRepository.save(defaultTheme);
    }

    @DisplayName("지나지 않은 시간에 대한 예약을 생성할 수 있다.")
    @Test
    void createFutureReservationTest() {
        //when
        ReservationResponse saved = reservationService.save(
                loginMember,
                new ReservationRequest(
                        LocalDate.now().plusDays(1),
                        defaultTime.getId(),
                        defaultTheme.getId()
                ));

        //then
        assertAll(
                () -> assertThat(reservationRepository.findAll().getReservations())
                        .hasSize(1),
                () -> assertThat(saved.id()).isEqualTo(1L)
        );
    }

    @DisplayName("지난 시간에 대해 예약을 시도할 경우 예외가 발생한다.")
    @Test
    void createPastReservationFailTest() {
        assertThatThrownBy(() -> reservationService.save(
                loginMember,
                new ReservationRequest(
                        LocalDate.now().minusDays(1),
                        defaultTime.getId(),
                        defaultTheme.getId()
                )))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(PAST_TIME_RESERVATION.getMessage());
    }

    @DisplayName("존재하지 않는 시간에 대해 예약을 생성하면 예외가 발생한다.")
    @Test
    void createReservationWithTimeNotExistsTest() {
        assertThatThrownBy(() -> reservationService.save(
                loginMember,
                new ReservationRequest(
                        LocalDate.now().minusDays(1),
                        2L,
                        defaultTheme.getId()
                )))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @DisplayName("존재하지 않는 테마에 대해 예약을 생성하면 예외가 발생한다.")
    @Test
    void createReservationWithThemeNotExistsTest() {
        assertThatThrownBy(() -> reservationService.save(
                loginMember,
                new ReservationRequest(
                        LocalDate.now().plusDays(1),
                        defaultTime.getId(),
                        2L
                )))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_THEME.getMessage());
    }

    @DisplayName("예약이 여러 개 존재하는 경우 모든 예약을 조회할 수 있다.")
    @Test
    void findAllTest() {
        //given
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(1), defaultTime, defaultTheme,
                loginMember));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(2), defaultTime, defaultTheme,
                loginMember));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(3), defaultTime, defaultTheme,
                loginMember));
        reservationRepository.save(new Reservation(LocalDate.now().plusDays(4), defaultTime, defaultTheme,
                loginMember));

        //when
        List<ReservationResponse> reservationResponses = reservationService.findAll();

        //then
        assertThat(reservationResponses).hasSize(4);
    }

    @DisplayName("예약이 하나 존재하는 경우")
    @Nested
    class OneReservationExistsTest {

        LocalDate defaultDate = LocalDate.now().plusDays(1);
        Reservation defaultReservation;

        @BeforeEach
        void addDefaultReservation() {
            defaultReservation = new Reservation(defaultDate, defaultTime, defaultTheme, loginMember);
            defaultReservation = reservationRepository.save(defaultReservation);
        }

        @DisplayName("이미 예약된 시간, 테마의 예약을 또 생성할 수 없다.")
        @Test
        void duplicatedReservationFailTest() {
            assertThatThrownBy(() -> reservationService.save(
                    loginMember,
                    new ReservationRequest(defaultDate, defaultTime.getId(), defaultTheme.getId())))
                    .isInstanceOf(RoomescapeException.class)
                    .hasMessage(DUPLICATE_RESERVATION.getMessage());
        }

        @DisplayName("예약을 삭제할 수 있다.")
        @Test
        void deleteReservationTest() {
            //when
            reservationService.delete(1L);

            //then
            assertThat(reservationRepository.findAll().getReservations()).isEmpty();
        }

        @DisplayName("존재하지 않는 예약에 대한 삭제 요청은 정상 요청으로 간주한다.")
        @Test
        void deleteNotExistReservationNotThrowsException() {
            assertThatCode(() -> reservationService.delete(2L))
                    .doesNotThrowAnyException();
        }
    }
}
