package roomescape.waiting;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.auth.AuthorizationException;
import roomescape.exception.custom.reason.reservation.*;
import roomescape.exception.custom.reason.waiting.WaitingNotFoundException;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@AllArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingResponse create(final WaitingRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());
        final Reservation savedReservation = saveReservation(request, reservationTime, member, theme);
        final ReservationResponse reservationResponse = ReservationResponse.from(savedReservation);

        final Long rank = getWaitingRank(request.date(), reservationTime, theme);
        final Waiting waiting = new Waiting(savedReservation, rank);
        final Waiting savedWaiting = waitingRepository.save(waiting);

        return WaitingResponse.of(savedWaiting, reservationResponse);
    }

    @Transactional
    public void deleteById(final Long reservationId, final LoginMember member) {
        Reservation reservation = getReservationById(reservationId);
        Waiting waiting = getWaitingByReservation(reservation);

        Member waitingMember = reservation.getMember();
        if (!isAuthorized(member, waitingMember)) {
            throw new AuthorizationException();
        }

        waitingRepository.delete(waiting);
        reservationRepository.delete(reservation);

        Long rank = waiting.getRank();
        List<Waiting> waitingsAfterRank = waitingRepository.findWaitingGreaterThanRank(
                waiting.getReservation().getDate(),
                waiting.getReservation().getReservationTime(),
                waiting.getReservation().getTheme(),
                rank);
        waitingsAfterRank.forEach(Waiting::decrementRank);
    }

    private Reservation saveReservation(final WaitingRequest request, final ReservationTime reservationTime, final Member member, final Theme theme) {
        validatePastDateTime(request.date(), reservationTime);
        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme, ReservationStatus.WAITING);
        return reservationRepository.save(notSavedReservation);
    }

    private boolean isAuthorized(final LoginMember member, final Member waitingMember) {
        return member.role().isAdmin() || waitingMember.isEmailEquals(member.email());
    }

    private Long getWaitingRank(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        final List<Reservation> waitings = reservationRepository.findAllByDateAndReservationTimeAndThemeAndReservationStatus(date, reservationTime, theme, ReservationStatus.WAITING);
        return (long) waitings.size();
    }

    private void validatePastDateTime(final LocalDate date, ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new ReservationPastDateException();
        }
        if (date.isEqual(today)) {
            validatePastTime(reservationTime);
        }
    }

    private void validatePastTime(final ReservationTime reservationTime) {
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new ReservationPastTimeException();
        }
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(ReservationNotExistsThemeException::new);
    }

    private ReservationTime getReservationTimeById(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(ReservationNotExistsTimeException::new);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private Reservation getReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private Waiting getWaitingByReservation(final Reservation reservation) {
        return waitingRepository.findByReservation(reservation)
                .orElseThrow(WaitingNotFoundException::new);
    }
}
