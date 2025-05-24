package roomescape.waiting.servcie;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.dto.WaitingCreateRequest;
import roomescape.waiting.dto.WaitingCreateResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public WaitingCreateResponse create(WaitingCreateRequest waitingCreateRequest, Member member) {
        Waiting waiting = buildWaiting(waitingCreateRequest, member);
        validateWaiting(waiting);
        waitingRepository.save(waiting);
        return WaitingCreateResponse.success(waitingRepository.countRankByCreateAt(waiting));
    }

    @Transactional
    public void delete(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.WAITING_NOTFOUND));
        waitingRepository.delete(waiting);
    }

    private Waiting buildWaiting(WaitingCreateRequest waitingCreateRequest, Member member) {
        ReservationTime time = findReservationTimeById(waitingCreateRequest.time());
        Theme theme = findThemeById(waitingCreateRequest.theme());
        validateDateTime(LocalDateTime.of(waitingCreateRequest.date(), time.getStartAt()));
        return new Waiting(waitingCreateRequest.date(), member, theme, time, LocalDateTime.now());
    }

    private Theme findThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.THEME_NOTFOUND));
    }

    private ReservationTime findReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND));
    }

    private void validateWaiting(Waiting waiting) {
        validateNoDuplicateReservation(waiting);
        validateNoDuplicateWaiting(waiting);
    }

    private void validateNoDuplicateWaiting(Waiting waiting) {
        if (waitingRepository.existsByMemberAndDateAndTime(waiting.getMember(), waiting.getDate(), waiting.getTime())) {
            throw new BadRequestException(ExceptionCause.WAITING_TIME_AND_DATE_DUPLICATE);
        }
    }

    private void validateNoDuplicateReservation(Waiting waiting) {
        if (reservationRepository.existsByMemberAndDateAndTime(waiting.getMember(), waiting.getDate(),
                waiting.getTime())) {
            throw new BadRequestException(ExceptionCause.RESERVATION_TIME_AND_DATE_DUPLICATE);
        }
    }

    private void validateDateTime(LocalDateTime requestDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (requestDateTime.isBefore(currentDateTime) || requestDateTime.equals(currentDateTime)) {
            throw new BadRequestException(ExceptionCause.RESERVATION_INVALID_FOR_PAST);
        }
    }
}
