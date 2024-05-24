package roomescape.service;

import static roomescape.domain.ReservationStatus.RESERVED;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.wait.WaitRequest;
import roomescape.service.dto.response.wait.WaitResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationWaitService {
    private final ReservationWaitRepository waitRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;

    public List<WaitResponse> findAllByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);

        return waitRepository.findAllByMember(member)
                .stream()
                .map(wait -> WaitResponse.from(wait, waitRepository.countByPriorityBefore(wait.getPriority())))
                .toList();
    }

    @Transactional
    public void saveReservationWait(WaitRequest request, long memberId) {
        Reservation reservation = getReservation(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);

        long waitCount = waitRepository.findPriorityIndex()
                .orElse(RESERVED.getStartIndex());

        ReservationWait wait = new ReservationWait(member, reservation, waitCount + 1L);
        verifyWait(wait);
        waitRepository.save(wait);
    }

    private Reservation getReservation(WaitRequest request) {
        List<Reservation> reservations = reservationRepository.findByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId());

        if (reservations.isEmpty()) {
            Theme theme = themeRepository.findById(request.themeId())
                    .orElseThrow(NotFoundTimeException::new);
            ReservationTime time = timeRepository.findById(request.timeId())
                    .orElseThrow(NotFoundTimeException::new);
            Reservation reservation = request.toReservation(time, theme);
            reservationRepository.save(reservation);
            return reservation;
        }

        return reservations.get(0);
    }

    private void verifyWait(ReservationWait wait) {
        List<ReservationWait> alreadySavedWaits = waitRepository.findByMemberAndReservation(wait.getMember(),
                wait.getReservation());
        wait.validateDuplicateWait(alreadySavedWaits);
    }

    @Transactional
    public void deleteReservationWait(Long reservationId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(NotFoundReservationException::new);
        waitRepository.deleteByMemberAndReservation(member, reservation);
    }
}
