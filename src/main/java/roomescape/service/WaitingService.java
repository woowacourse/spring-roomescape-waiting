package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private static final LocalDate NOW = LocalDate.now();

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long register(final WaitingRequest waitingRequest, final LoginMember loginMember) {
        final Reservation reservation = findReservationBy(
                waitingRequest.date(),
                waitingRequest.theme(),
                waitingRequest.time()
        );
        final Member member = findMember(loginMember.id());
        final Waiting waiting = createWaiting(reservation, member);
        validateExists(waiting, member);
        validateAfter(waiting);

        final Waiting savedWaiting = waitingRepository.save(waiting);
        return savedWaiting.getId();
    }

    private Reservation findReservationBy(final LocalDate date, final Long themeId, final Long timeId) {
        return reservationRepository.findByDateAndThemeIdAndReservationTimeId(
                date,
                themeId,
                timeId
        ).orElseThrow(() ->
                new NotFoundException("아직 예약 가능한 상태입니다. 예약이 완료된 시간에만 대기 신청이 가능합니다."));
    }

    private Member findMember(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("회원이 존재하지 않습니다."));
    }

    private Waiting createWaiting(final Reservation reservation, final Member member) {
        return Waiting.of(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getReservationTime(),
                member
        );
    }

    private void validateExists(final Waiting waiting, final Member member) {
        if (waitingRepository.existsByDateAndThemeAndReservationTimeAndMember(
                waiting.getDate(),
                waiting.getTheme(),
                waiting.getReservationTime(),
                member
        )) {
            throw new DuplicatedException("이미 대기 중인 예약입니다.");
        }
    }

    private void validateAfter(final Waiting waiting) {
        waiting.isAfterBy(NOW);
    }
}
