package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.NotFoundException;
import roomescape.dto.LoginMember;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.Waiting;
import roomescape.model.time.TimeProvider;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class WaitingService {

    private static final LocalDate NOW = LocalDate.now();

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeProvider timeProvider;

    public List<WaitingResponse> findAllWaiting() {
        final List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @Transactional
    public Long register(final WaitingRequest waitingRequest, final LoginMember loginMember) {
        final Reservation reservation = findReservationBy(
                waitingRequest.date(),
                waitingRequest.theme(),
                waitingRequest.time()
        );
        final Member member = findMember(loginMember.id());
        final LocalDateTime currentDateTime = timeProvider.getCurrentDateTime();
        final Waiting waiting = createWaiting(reservation, member, currentDateTime);

        validateExists(waiting, member);
        waiting.isAfterBy(NOW);

        final Waiting savedWaiting = waitingRepository.save(waiting);
        return savedWaiting.getId();
    }

    @Transactional
    public void cancel(final Long waitingId) {
        final Waiting waiting = findWaiting(waitingId);
        waitingRepository.delete(waiting);
    }

    private Waiting findWaiting(final Long waitingId) {
        return waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException("대기 신청이 존재하지 않습니다."));
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

    private Waiting createWaiting(final Reservation reservation, final Member member,
                                  final LocalDateTime currentDateTime) {
        return Waiting.of(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getReservationTime(),
                member,
                currentDateTime
        );
    }

    private void validateExists(final Waiting waiting, final Member member) {
        if (isExists(waiting, member)) {
            throw new DuplicatedException("이미 대기 중인 예약입니다.");
        }
    }

    private boolean isExists(final Waiting waiting, final Member member) {
        return waitingRepository.existsByDateAndThemeIdAndReservationTimeIdAndMemberId(
                waiting.getDate(),
                waiting.getTheme().getId(),
                waiting.getReservationTime().getId(),
                member.getId()
        );
    }
}
