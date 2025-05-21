package roomescape.service.waiting;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.dto.reservation.MemberReservationResponse;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.waiting.WaitingResponse;
import roomescape.dto.waiting.WaitingWithRankResponse;
import roomescape.exception.reservation.ReservationInPastException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.theme.ThemeNotFoundException;
import roomescape.exception.waiting.ReservationWaitingDuplicateException;
import roomescape.exception.waiting.WaitingAlreadyExistException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.repository.theme.ThemeRepository;
import roomescape.repository.waiting.WaitingRepsitory;

@Service
public class WaitingService {

    private final WaitingRepsitory waitingRepsitory;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepsitory waitingRepsitory, ReservationTimeRepository timeRepository,
                          ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepsitory = waitingRepsitory;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

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

        if (waitingRepsitory.existsByDateAndTimeIdAndThemeIdAndMemberId(request.date(), request.timeId(),
                request.themeId(), member.getId())) {
            throw new WaitingAlreadyExistException();
        }

        Waiting waiting = new Waiting(null, request.date(), reservationTime, theme, member);

        return WaitingResponse.from(waitingRepsitory.save(waiting));
    }

    public List<MemberReservationResponse> getWaitingByMember(Member member) {

        List<WaitingWithRank> waitings = waitingRepsitory.findWaitingsWithRankByMemberId(member.getId());

        return waitings.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    public void deleteWaitingById(Long id) {

        waitingRepsitory.deleteById(id);
    }

    public List<WaitingWithRankResponse> findAllWaitings() {

        List<WaitingWithRank> waitings = waitingRepsitory.findAllWithRank();
        return waitings.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }
}
