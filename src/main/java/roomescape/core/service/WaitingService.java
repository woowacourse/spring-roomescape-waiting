package roomescape.core.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Member;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.dto.waiting.WaitingRequest;
import roomescape.core.dto.waiting.WaitingResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.core.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(final WaitingRepository waitingRepository, final MemberRepository memberRepository,
                          final ReservationTimeRepository reservationTimeRepository,
                          final ThemeRepository themeRepository,
                          final ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse create(final WaitingRequest request) {
        final Waiting waiting = createWaiting(request);

        validateDuplicatedReservation(waiting);

        return new WaitingResponse(waitingRepository.save(waiting));
    }

    private void validateDuplicatedReservation(final Waiting waiting) {
        final Integer reservationCount = reservationRepository.countByMemberAndDateAndTimeAndTheme(waiting.getMember(),
                waiting.getDate(), waiting.getTime(), waiting.getTheme());
        if (reservationCount > 0) {
            throw new IllegalArgumentException("해당 시간에 이미 예약한 내역이 존재합니다. 예약 대기할 수 없습니다.");
        }
    }

    private Waiting createWaiting(final WaitingRequest request) {
        final Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        final ReservationTime reservationTime = reservationTimeRepository.findById(request.getTimeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));
        final Theme theme = themeRepository.findById(request.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));

        return new Waiting(member, request.getDate(), reservationTime, theme);
    }

    @Transactional(readOnly = true)
    public List<WaitingResponse> findAll() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final long id) {
        waitingRepository.deleteById(id);
    }
}
