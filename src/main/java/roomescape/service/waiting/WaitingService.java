package roomescape.service.waiting;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.dto.waiting.WaitingWithMemberNameResponse;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.exception.waiting.ReservationWaitingDuplicateException;
import roomescape.exception.waiting.WaitingAlreadyExistException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository timeRepository,
                          ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public WaitingResponse create(ReservationRequest request, Member member) {
        ReservationTime reservationTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException(request.timeId()));

        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new ThemeNotFoundException(request.themeId()));

        if (LocalDateTime.now().isAfter(LocalDateTime.of(request.date(), reservationTime.getStartAt()))) {
            throw new ReservationInPastException();
        }

        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), member.getId())) {
            throw new ReservationWaitingDuplicateException();
        }

        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), member.getId())) {
            throw new WaitingAlreadyExistException();
        }

        Waiting waiting = new Waiting(null, request.date(), reservationTime, theme, member);

        return WaitingResponse.from(waitingRepository.save(waiting));
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findWaitingWithRankByMember(Member member) {

        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(member.getId());

        return waitings.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public void deleteWaitingById(Long id) {

        waitingRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<WaitingWithMemberNameResponse> findAllWaitings() {

        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingWithMemberNameResponse::from)
                .toList();
    }
}
