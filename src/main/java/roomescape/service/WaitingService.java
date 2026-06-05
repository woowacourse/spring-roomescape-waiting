package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingCreateCommand;
import roomescape.service.dto.WaitingResult;

@Service
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

    public WaitingResult create(WaitingCreateCommand command) {
        ReservationTime time = reservationTimeRepository.findById(command.getTimeId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeRepository.findById(command.getThemeId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));
        Slot slot = new Slot(command.getDate(), time, theme);

        Reservation reservation = reservationRepository.findBySlot(
                command.getDate(),
                command.getTimeId(),
                command.getThemeId()
        ).orElseThrow(() -> new BusinessRuleViolationException("예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요."));

        if (reservation.isOwnedBy(command.getName())) {
            throw new BusinessRuleViolationException("이미 본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }

        Waitings existing = new Waitings(
                waitingRepository.findBySlot(command.getDate(), time.getId(), theme.getId())
        );
        existing.validateNoDuplicateBy(command.getName());

        Waiting newWaiting = Waiting.create(
                command.getName(), slot, existing.nextOrderIndex()
        );
        Waiting saved = waitingRepository.save(newWaiting);
        return WaitingResult.from(saved);
    }

    @Transactional
    public void cancelByOwner(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id)
                .filter(w -> w.isOwnedBy(name))
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 대기입니다."));

        waitingRepository.deleteById(id);

        Waitings remaining = new Waitings(
                waitingRepository.findBySlot(
                        waiting.getDate(),
                        waiting.getTime().getId(),
                        waiting.getTheme().getId())
        );
        for (Waiting w : remaining.reorderAfterRemoval(waiting.getOrderIndex())) {
            waitingRepository.updateOrderIndex(w.getId(), w.getOrderIndex());
        }
    }
}
