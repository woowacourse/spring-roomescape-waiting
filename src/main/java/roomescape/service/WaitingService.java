package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.WaitingResponse;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<WaitingResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAllByStatus(ReservationStatus.WAITING);
        return waitings.stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public WaitingResponse save(WaitingRequest waitingRequest, Member member) {
        ReservationTime reservationTime = findReservationTimeById(waitingRequest.timeId());
        Theme theme = findThemeById(waitingRequest.themeId());

        Waiting waiting = waitingRequest.toWaiting(member, reservationTime, theme);
        validateDuplicateWaiting(waitingRequest, member);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    @Transactional
    public WaitingResponse denyWaiting(Long id) {
        Waiting waiting = findWaitingById(id);
        waiting.denyWaiting();
        return new WaitingResponse(waiting);
    }

    @Transactional
    public void delete(Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        waitingRepository.delete(waiting);
    }

    private void validateDuplicateWaiting(WaitingRequest request, Member member) {
        if (reservationRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(), request.date(), request.timeId(), request.themeId())) {
            throw new DuplicatedReservationException();
        }
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                request.date(), request.timeId(), request.themeId(), member.getId())) {
            throw new DuplicatedReservationException();
        }
    }

    private Waiting findWaitingById(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    private ReservationTime findReservationTimeById(long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(NotFoundTimeException::new);
    }

    private Theme findThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }
}
