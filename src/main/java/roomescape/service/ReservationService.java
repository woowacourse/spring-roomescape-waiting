package roomescape.service;

import static roomescape.domain.ReservationStatus.RESERVE_NUMBER;
import static roomescape.domain.ReservationStatus.Status.RESERVED;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.policy.CurrentDueTimePolicy;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.wait.AdminReservationResponse;
import roomescape.service.dto.response.wait.ReservationWithStatusResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public List<ReservationResponse> findAllReservation() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCond cond) {
        return reservationRepository.findByPeriodAndThemeAndMember(
                        cond.start(),
                        cond.end(),
                        cond.memberName(),
                        cond.themeName(),
                        RESERVED)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationRequest request) {
        Reservation reservation = createReservation(request);
        Reservation verifiedReservation = verifyReservation(reservation);
        reservationRepository.save(verifiedReservation);

        return new ReservationResponse(verifiedReservation);
    }

    private Reservation createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(NotFoundTimeException::new);
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(NotFoundThemeException::new);
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(AuthenticationFailureException::new);
        Long nextPriority = reservationRepository.findHighestPriority(request.date(), theme, time)
                .map(Reservation::getNextPriority)
                .orElse(RESERVE_NUMBER);

        ReservationStatus status = ReservationStatus.from(nextPriority);
        return new Reservation(request.date(), time, theme, status, member);
    }

    private Reservation verifyReservation(Reservation reservation) {
        reservation.validateDateTimeReservation(new CurrentDueTimePolicy());
        reservationRepository.findByDateAndTimeAndThemeAndMember(
                        reservation.getDate(),
                        reservation.getTime(),
                        reservation.getTheme(),
                        reservation.getMember())
                .ifPresent(v -> {
                    throw new DuplicatedReservationException();
                });
        return reservation;
    }

    public List<AdminReservationResponse> findAllWaits() {
        return reservationRepository.findAll()
                .stream()
                .map(AdminReservationResponse::new)
                .toList();
    }


    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(NotFoundReservationException::new);

        reservationRepository.delete(reservation);
        proceedAutoScheduling(reservation);
    }

    private void proceedAutoScheduling(Reservation reservation) {
        if (reservation.isReserved()) {
            reservationRepository.findTopByDateAndThemeAndTimeOrderByStatusPriorityAsc(
                            reservation.getDate(),
                            reservation.getTheme(),
                            reservation.getTime())
                    .ifPresentOrElse(
                            Reservation::reserve,
                            () -> reservationRepository.delete(reservation));
        }
    }

    public List<ReservationWithStatusResponse> findAllByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(reservation -> ReservationWithStatusResponse.from(reservation, findRank(reservation)))
                .toList();
    }

    private long findRank(Reservation reservation) {
        return reservationRepository.countByDateAndThemeAndTimeAndStatusPriorityIsLessThan(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime(),
                reservation.getPriority());

    }
}
