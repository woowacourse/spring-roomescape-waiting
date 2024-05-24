package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
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
    private final Clock clock;
    private final ReservationRepository reservationRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository, Clock clock, ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
        this.reservationRepository = reservationRepository;
    }

    public List<WaitingResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::new)
                .toList();
    }

    @Transactional
    public WaitingResponse save(WaitingRequest waitingRequest, Member member) {
        ReservationTime reservationTime = findReservationTimeById(waitingRequest.getTimeId());
        Theme theme = findThemeById(waitingRequest.getThemeId());
        Waiting waiting = waitingRequest.toWaiting(member, reservationTime, theme);

        validateDuplicateWaiting(waitingRequest, member);
        validateDateTimeWaiting(waitingRequest, reservationTime);

        Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    @Transactional
    public void delete(Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        waitingRepository.delete(waiting);
    }

    private void validateDuplicateWaiting(WaitingRequest request, Member member) {
        if (waitingRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                request.getDate(), request.getTimeId(), request.getThemeId(), member.getId())) {
            throw new DuplicatedReservationException();
        }
    }

    private void validateDateTimeWaiting(WaitingRequest request, ReservationTime time) {
        LocalDateTime localDateTime = request.getDate().atTime(time.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now(clock))) {
            throw new InvalidDateTimeReservationException();
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
