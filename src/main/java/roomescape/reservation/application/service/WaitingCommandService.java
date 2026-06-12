package roomescape.reservation.application.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class WaitingCommandService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationApplicationResult save(ReservationApplicationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        ReservationSlot slot = request.toSlot(time.getStartAt());
        slot.validateReservable(request.now());

        if (!reservationRepository.existsBySlot(slot)) {
            throw new RoomEscapeException("예약이 존재하지 않는 경우, 대기를 신청할 수 없습니다.");
        }

        Waiting waiting = request.toWaiting(slot);
        if (waitingRepository.existsByMemberNameAndSlot(waiting.getMemberName(), slot)) {
            throw new ConflictException("이미 해당 테마의 날짜와 시간에 대기를 신청했습니다.");
        }

        Waiting savedWaiting = waitingRepository.save(waiting);
        Long rank = waitingRepository.getRank(savedWaiting);
        return ReservationApplicationResult.waiting(
                savedWaiting,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time),
                rank
        );
    }

    public void cancel(Long id, LocalDateTime now) {
        ReservationSlot slot = waitingRepository.findSlotById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 대기입니다."));

        slot.validateDeletable(now);
        waitingRepository.delete(id);
    }
}
