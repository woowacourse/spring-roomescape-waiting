package roomescape.service;

import static roomescape.domain.ReservationStatus.RESERVE_NUMBER;
import static roomescape.domain.ReservationStatus.Status.RESERVED;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.policy.CurrentDueTimePolicy;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.reservation.ReservationRequest;
import roomescape.service.dto.request.reservation.ReservationSearchCond;
import roomescape.service.dto.response.reservation.ReservationResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitRepository waitRepository;

    public List<ReservationResponse> findAllReservation() {
        List<ReservationWait> waits = waitRepository.findAll();

        return waits.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllReservationByConditions(ReservationSearchCond cond) {
        return waitRepository.findByPeriodAndMemberAndThemeAndStatus(cond.start(), cond.end(), cond.memberName(),
                        cond.themeName(), RESERVED)
                .stream()
                .map(ReservationWait::getReservation)
                .map(reservation -> ReservationResponse.from(reservation, cond.memberName()))
                .toList();
    }

    @Transactional
    public ReservationResponse saveReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(NotFoundTimeException::new);
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(NotFoundThemeException::new);
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(AuthenticationFailureException::new);

        Reservation verifiedReservation = verifyReservation(request, time, theme);
        Reservation savedReservation = reservationRepository.save(verifiedReservation);

        ReservationWait wait = waitRepository.save(new ReservationWait(member, savedReservation, RESERVE_NUMBER));
        return ReservationResponse.from(wait);
    }

    private Reservation verifyReservation(ReservationRequest request, ReservationTime time, Theme theme) {
        Reservation reservation = request.toReservation(time, theme);
        reservation.validateDateTimeReservation(new CurrentDueTimePolicy());
        reservationRepository.findByDateAndTimeAndTheme(request.date(), time, theme)
                .ifPresent(v -> {
                    throw new DuplicatedReservationException();
                });
        return reservation;
    }

    @Transactional
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(NotFoundReservationException::new);

        waitRepository.deleteByReservation(reservation);
        reservationRepository.delete(reservation);
    }
}
