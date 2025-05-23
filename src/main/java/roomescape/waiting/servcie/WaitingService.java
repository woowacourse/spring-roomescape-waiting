package roomescape.waiting.servcie;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
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
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public WaitingCreateResponse create(WaitingCreateRequest waitingCreateRequest, Member member) {
        Waiting waiting = createWaiting(waitingCreateRequest, member);
        validateWaiting(waiting);
        waitingRepository.save(waiting);
        return WaitingCreateResponse.success(waitingRepository.countRankByCreateAt(waiting));
    }

    public List<WaitingWithRank> findAllByMemberWithRank(Long memberId) {
        List<Waiting> waitings = waitingRepository.findAllByMemberId(memberId);
        return waitings.stream()
                .map(waiting -> new WaitingWithRank(waiting, waitingRepository.countRankByCreateAt(waiting)))
                .toList();
    }

    public void delete(Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new NotFoundException(ExceptionCause.WAITING_NOTFOUND));
        waitingRepository.delete(waiting);
    }

    private void validateWaiting(Waiting waiting) {
        if(reservationRepository.existsByMemberAndDateAndTime(waiting.getMember(), waiting.getDate(), waiting.getTime())) {
            throw new BadRequestException(ExceptionCause.RESERVATION_TIME_AND_DATE_DUPLICATE);
        }
        if(waitingRepository.existsByMemberAndDateAndTime(waiting.getMember(), waiting.getDate(), waiting.getTime())) {
            throw new BadRequestException(ExceptionCause.WAITING_TIME_AND_DATE_DUPLICATE);
        }
    }

    private Waiting createWaiting(WaitingCreateRequest waitingCreateRequest, Member member) {
        ReservationTime time = reservationTimeRepository.findById(waitingCreateRequest.time())
                .orElseThrow(() -> new NotFoundException(ExceptionCause.RESERVATION_TIME_NOTFOUND));
        Theme theme = themeRepository.findById(waitingCreateRequest.theme())
                .orElseThrow(() -> new NotFoundException(ExceptionCause.THEME_NOTFOUND));
        validateDateTime(LocalDateTime.of(waitingCreateRequest.date(), time.getStartAt()));
        return new Waiting(waitingCreateRequest.date(), member, theme, time, LocalDateTime.now());
    }

    private void validateDateTime(LocalDateTime requestDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (requestDateTime.isBefore(currentDateTime) || requestDateTime.equals(currentDateTime)) {
            throw new BadRequestException(ExceptionCause.RESERVATION_INVALID_FOR_PAST);
        }
    }
}
