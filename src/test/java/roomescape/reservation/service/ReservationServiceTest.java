package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.fixture.MemberFixture;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFixture;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.MyReservationJsonResponse;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFixture;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixture;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoBean
    private ReservationRepository reservationRepository;

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
        List<ReservationTime> availableTimes = List.of(
            reservationTime1,
            reservationTime2,
            reservationTime3,
            reservationTimeToReserve
        );
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(
            reservationDate,
            reservationTimeToReserve.getId(),
            theme.getId()
        );

        Reservation reservationWillBeSaved = Reservation.createFirstWaiting(
            reservationCreateRequest.date(),
            reservationTimeToReserve,
            theme,
            member
        );

        Reservation savedReservation = new Reservation(
            1L,
            reservationDate,
            reservationTimeToReserve,
            theme,
            member,
            1
        );

        when(reservationRepository.save(reservationWillBeSaved))
            .thenReturn(savedReservation);

        // when
        ReservationResponse actual = reservationService.create(
            reservationTimeToReserve,
            theme,
            member,
            availableTimes,
            reservationCreateRequest
        );

        ReservationResponse expected = ReservationResponse.fromReservation(savedReservation);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 모든_예약을_조회할_수_있다() {

        // given
        List<Reservation> reservations = IntStream.range(0, 5)
            .mapToObj(i -> ReservationFixture.create())
            .toList();

        List<ReservationResponse> expected = reservations.stream()
            .map(ReservationResponse::fromReservation)
            .toList();

        when(reservationRepository.findAll())
            .thenReturn(reservations);

        // when
        List<ReservationResponse> actual = reservationService.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 조건에_따라_예약을_조회할_수_있다() {

        // given
        List<Reservation> reservations = IntStream.range(0, 5)
            .mapToObj(i -> ReservationFixture.create())
            .toList();

        List<ReservationResponse> expected = reservations.stream()
            .map(ReservationResponse::fromReservation)
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

        when(
            reservationRepository.findByMemberAndThemeAndVisitDateBetween(
                themeId,
                memberId,
                dateFrom,
                dateTo
            )
        ).thenReturn(reservations);

        // when
        List<ReservationResponse> actual = reservationService.findByCondition(request);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        Reservation reservation = ReservationFixture.create();

        // when
        reservationService.deleteById(reservation.getId());

        // then
        verify(reservationRepository).deleteById(reservation.getId());
    }

    @Test
    void 예약_시간_id애_대한_예약이_존재하지_않는지_검증한다() {

        // given
        long reservationTimeId = 1L;

        when(reservationRepository.existsByTimeId(reservationTimeId))
            .thenReturn(false);

        // when & then
        assertThatCode(() -> reservationService.validateReservationNonExistenceByTimeId(reservationTimeId))
            .doesNotThrowAnyException();
    }

    @Test
    void 멤버를_통해_모든_예약을_조회할_수_있다() {

        // given
        Member member = MemberFixture.create(MemberRole.USER);
        List<Reservation> reservations = IntStream.range(0, 5)
            .mapToObj(i -> ReservationFixture.create())
            .toList();
        List<MyReservationResponse> expected = reservations.stream()
            .map(reservation -> MyReservationJsonResponse.fromReservationAndStatus(reservation, "0번째 예약대기"))
            .collect(Collectors.toList());

        when(reservationRepository.findAllByMember(member))
            .thenReturn(reservations);

        // when
        List<MyReservationResponse> actual = reservationService.findAllByMember(member);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }
}
