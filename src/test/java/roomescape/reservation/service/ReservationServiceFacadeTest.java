package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.auth.infrastructure.methodargument.MemberPrincipal;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFixture;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationJsonResponse;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixture;
import roomescape.theme.service.ThemeService;

@SpringBootTest
class ReservationServiceFacadeTest {

    @Autowired
    private ReservationServiceFacade reservationServiceFacade;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 예약을_생성할_수_있다() {

        // given
        ReservationTime reservationTime1 = ReservationTimeFixture.createWithoutId();
        ReservationTime reservationTime2 = ReservationTimeFixture.createWithoutId();
        ReservationTime reservationTime3 = ReservationTimeFixture.createWithoutId();

        LocalDate reservationDate = LocalDate.now().plusDays(3);
        ReservationTime reservationTimeToReserve = ReservationTimeFixture.createWithoutId();
        Theme theme = ThemeFixture.createWithoutId();
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        MemberPrincipal memberPrincipal = new MemberPrincipal(member.getName());
        List<ReservationTime> availableTimes = List.of(
            reservationTime1,
            reservationTime2,
            reservationTime3,
            reservationTimeToReserve
        );
        ReservationCreateRequest request = new ReservationCreateRequest(
            reservationDate,
            reservationTimeToReserve.getId(),
            theme.getId()
        );

        Reservation savedReservation = new Reservation(
            1L,
            reservationDate,
            reservationTimeToReserve,
            theme,
            member,
            1
        );

        when(reservationTimeService.findByIdOrThrow(reservationTimeToReserve.getId()))
            .thenReturn(reservationTimeToReserve);

        when(themeService.findByIdOrThrow(theme.getId()))
            .thenReturn(theme);

        when(memberService.findByPrincipalOrThrow(memberPrincipal))
            .thenReturn(member);

        when(reservationTimeService.findByReservationDateAndThemeId(
                request.date(),
                request.themeId()
            )
        ).thenReturn(availableTimes);

        when(reservationService.create(
                reservationTimeToReserve,
                reservationDate,
                theme,
                member,
                availableTimes
            )
        ).thenReturn(ReservationResponse.fromReservation(savedReservation));

        ReservationResponse expected = ReservationResponse.fromReservation(savedReservation);

        // when
        ReservationResponse actual = reservationServiceFacade.createReservation(request, memberPrincipal);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_예약을_조회할_수_있다() {

        // given
        Reservation reservation1 = ReservationFixture.create();
        Reservation reservation2 = ReservationFixture.create();
        List<ReservationResponse> expected = Stream.of(reservation1, reservation2)
            .map(ReservationResponse::fromReservation)
            .toList();

        when(reservationService.findAll()).thenReturn(expected);

        // when
        List<ReservationResponse> actual = reservationServiceFacade.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 예약을_삭제할_수_있다() {

        // when
        reservationServiceFacade.deleteById(1L);

        // then
        verify(reservationService)
            .deleteById(1L);
    }

    @Test
    void 내_예약을_조회할_수_있다() {

        // given
        Member member = MemberFixture.createWithoutId(MemberRole.USER);
        MemberPrincipal memberPrincipal = new MemberPrincipal(member.getName());
        Reservation reservation1 = ReservationFixture.create();
        Reservation reservation2 = ReservationFixture.create();
        List<MyReservationResponse> expected = Stream.of(reservation1, reservation2)
            .map(reservation -> MyReservationJsonResponse.fromReservationAndStatus(reservation, "예약"))
            .collect(Collectors.toList());

        when(memberService.findByPrincipalOrThrow(memberPrincipal))
            .thenReturn(member);

        when(reservationService.findAllByMember(member)).thenReturn(expected);

        // when
        List<MyReservationResponse> actual = reservationServiceFacade.findMine(memberPrincipal);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
