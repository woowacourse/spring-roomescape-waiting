package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationSchedule;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.global.dto.SessionMember;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.request.WaitingCreateRequest;
import roomescape.service.response.WaitingResponse;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;
    private final ReservationRepository reservationRepository;

    public WaitingService(
            final WaitingRepository waitingRepository,
            final MemberRepository memberRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final Clock clock,
            final ReservationRepository reservationRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse createWaiting(final WaitingCreateRequest request, final Long memberId) {
        validateNoDuplicateReservationOrWaiting(request, memberId);
        final Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 멤버가 존재하지 않습니다."));
        final ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NoSuchElementException("해당 예약 시간이 존재하지 않습니다."));
        final Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NoSuchElementException("해당 테마가 존재하지 않습니다."));

        final Waiting waiting = new Waiting(
                null,
                member,
                new ReservationSchedule(new ReservationDate(request.date()), time, theme),
                LocalDateTime.now(clock)
        );

        return WaitingResponse.from(waitingRepository.save(waiting));
    }

    private void validateNoDuplicateReservationOrWaiting(final WaitingCreateRequest request, final Long memberId) {
        if (reservationRepository.existsByScheduleAndMemberId(request.date(), request.timeId(), request.themeId(), memberId)) {
            throw new IllegalStateException("이미 예약이 존재합니다.");
        }
        if (waitingRepository.existsByScheduleAndMemberId(request.date(), request.timeId(), request.themeId(), memberId)) {
            throw new IllegalStateException("이미 대기가 존재합니다.");
        }
    }

    public List<WaitingResponse> findAll() {
        return waitingRepository.findAll().stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void approveWaitingById(final Long id) {
        final Waiting oldWaiting = waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("예약 대기를 찾을 수 없습니다."));
        validateReservationAvailability(oldWaiting);

        final Reservation newReservation = new Reservation(
                null,
                oldWaiting.getMember(),
                oldWaiting.getSchedule()
        );

        reservationRepository.save(newReservation);
        waitingRepository.delete(oldWaiting);
    }

    private void validateReservationAvailability(final Waiting oldWaiting) {
        if (reservationRepository.existsBySchedule(oldWaiting.getSchedule())) {
            throw new IllegalStateException("이미 예약이 존재합니다.");
        }
    }

    public void deleteWaitingById(final Long id, final SessionMember sessionMember) {
        final Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("예약 대기 정보를 찾을 수 없습니다."));
        validateWaitingAccess(waiting, sessionMember);
        waitingRepository.delete(waiting);
    }

    private void validateWaitingAccess(final Waiting waiting, final SessionMember sessionMember) {
        final boolean isOwner = sessionMember.isSameMember(waiting.getMember().getId());
        final boolean isAdmin = sessionMember.isAdmin();
        if (!(isOwner || isAdmin)) {
            throw new IllegalStateException("해당 대기를 삭제할 권한이 없습니다.");
        }
    }
}
