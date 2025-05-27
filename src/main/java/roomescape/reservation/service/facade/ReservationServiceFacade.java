package roomescape.reservation.service.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.AvailableReservationTimeRequest;
import roomescape.reservation.dto.AvailableReservationTimeResponse;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.CreateReservationResponse;
import roomescape.reservation.dto.CreateWaitingRequest;
import roomescape.reservation.dto.CreateWaitingResponse;
import roomescape.reservation.dto.ReservationMineResponse;
import roomescape.reservation.service.reservation.ReservationService;
import roomescape.reservation.service.waiting.ReservationWaitingService;

@RequiredArgsConstructor
@Service
public class ReservationServiceFacade {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;
    private final MemberService memberService;

    @Transactional
    public CreateReservationResponse saveReservation(final CreateReservationRequest request,
                                                     final LoginMember loginMember) {
        final Member member = memberService.findMemberByEmail(loginMember.email());
        final LocalDate date = request.date();
        final Long timeId = request.timeId();
        final Long themeId = request.themeId();

        final Reservation savedReservation = reservationService.save(member, date, timeId, themeId);

        return CreateReservationResponse.from(savedReservation);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationService.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationMineResponse> findMyReservations(final LoginMember loginMember) {
        final Member member = memberService.findMemberByEmail(loginMember.email());
        final List<Reservation> reservations = reservationService.findByMember(member);
        final List<Waiting> waitings = reservationWaitingService.findWaitingByMember(member);

        return Stream.concat(
                reservations.stream().map(ReservationMineResponse::from),
                waitings.stream().map(waiting -> {
                    final long rank = reservationWaitingService.getRankInWaiting(waiting);
                    return ReservationMineResponse.from(waiting, rank);
                })
        ).toList();
    }

    @Transactional
    public CreateWaitingResponse saveWaiting(final CreateWaitingRequest request, final LoginMember loginMember) {
        final Member member = memberService.findMemberByEmail(loginMember.email());
        final LocalDate date = request.date();
        final Long time = request.time();
        final Long theme = request.theme();

        final Waiting savedWaiting = reservationWaitingService.createWaitingReservation(member, date, time, theme);

        return CreateWaitingResponse.from(savedWaiting);
    }

    @Transactional
    public void deleteWaiting(final Long id) {
        reservationWaitingService.deleteWaitingById(id);
    }

    @Transactional(readOnly = true)
    public List<CreateReservationResponse> findAll() {
        final List<Reservation> reservations = reservationService.findAll();

        return reservations.stream()
                .map(CreateReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> findAvailableTimes(final AvailableReservationTimeRequest request) {

        final LocalDate date = request.date();
        final Long themeId = request.themeId();

        return reservationService.findAvailableReservationTimes(date, themeId)
                .stream()
                .map(availableReservationTime -> new AvailableReservationTimeResponse(
                        availableReservationTime.id(),
                        availableReservationTime.startAt(),
                        availableReservationTime.alreadyBooked()
                ))
                .toList();
    }
}
