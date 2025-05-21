package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFixture;
import roomescape.reservation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixture;
import roomescape.theme.service.ThemeService;

@SpringBootTest
class AdminReservationFacadeTest {

    @Autowired
    AdminReservationFacade adminReservationFacade;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private ReservationTimeService reservationTimeService;

    @MockitoBean
    private ThemeService themeService;


    @Test
    void 관리자는_예악을_생성할_수_있다() {

        // given
        ReservationTime reservationTime1 = ReservationTimeFixture.createWithoutId();
        ReservationTime reservationTime2 = ReservationTimeFixture.createWithoutId();
        ReservationTime reservationTime3 = ReservationTimeFixture.createWithoutId();

        LocalDate reservationDate = LocalDate.now().plusDays(3);
        ReservationTime reservationTimeToReserve = ReservationTimeFixture.createWithoutId();
        Theme theme = ThemeFixture.createWithoutId();
        Member member = MemberFixture.createWithoutId(MemberRole.ADMIN);
        List<ReservationTime> availableTimes = List.of(
            reservationTime1,
            reservationTime2,
            reservationTime3,
            reservationTimeToReserve
        );
        AdminReservationCreateRequest request = new AdminReservationCreateRequest(
            reservationDate,
            reservationTimeToReserve.getId(),
            theme.getId(),
            member.getId()
        );

        Reservation savedReservation = new Reservation(
            1L,
            reservationDate,
            reservationTimeToReserve,
            theme,
            member
        );

        when(reservationTimeService.findById(reservationTimeToReserve.getId()))
            .thenReturn(Optional.of(reservationTimeToReserve));

        when(themeService.findById(theme.getId()))
            .thenReturn(Optional.of(theme));

        when(memberService.findExistingMemberById(member.getId()))
            .thenReturn(member);

        when(reservationTimeService.findByReservationDateAndThemeId(
                request.date(),
                request.themeId()
            )
        ).thenReturn(availableTimes);

        when(reservationService.createReservation(
                reservationTimeToReserve,
                theme,
                member,
                availableTimes,
                request.toReservationCreateRequest()
            )
        ).thenReturn(ReservationResponse.from(savedReservation));

        ReservationResponse expected = ReservationResponse.from(savedReservation);

        // when
        ReservationResponse actual = adminReservationFacade.create(request);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 관리자는_조건에_따라_예약을_조회할_수_있다() {

        // given
        List<Reservation> reservations = IntStream.range(0, 5)
            .mapToObj(i -> ReservationFixture.create())
            .toList();

        List<ReservationResponse> expected = reservations.stream()
            .map(ReservationResponse::from)
            .toList();

        long themeId = 1L;
        long memberId = 2L;
        LocalDate dateFrom = LocalDate.now();
        LocalDate dateTo = LocalDate.now().plusDays(1);

        ReservationSearchConditionRequest request = new ReservationSearchConditionRequest(
            themeId,
            memberId,
            dateFrom,
            dateTo
        );

        when(reservationService.findByCondition(request)).thenReturn(expected);

        // when
        List<ReservationResponse> actual = reservationService.findByCondition(request);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
