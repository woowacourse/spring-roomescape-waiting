package roomescape.application;

import org.springframework.stereotype.Service;
import roomescape.application.dto.command.WaitingCreateCommand;
import roomescape.application.dto.result.WaitingResult;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.Waitings;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.exception.client.ResourceNotFoundException;

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
        ReservationTime time = findTimeOrThrow(command.getTimeId());
        Theme theme = findThemeOrThrow(command.getThemeId());

        validateNotReservedBySelf(command);
        validateReservationExists(command);

        Waitings existing = new Waitings(
                waitingRepository.findBySlot(command.getDate(), time.getId(), theme.getId())
        );
        existing.validateNoDuplicateBy(command.getName());

        Waiting newWaiting = Waiting.create(
                command.getName(), command.getDate(), time, theme, existing.nextOrderIndex()
        );
        Waiting saved = waitingRepository.save(newWaiting);
        return WaitingResult.from(saved);
    }

    //TODO @Transactional은 사이클 2에서 다루는 내용이므로 사이클 1에서는 다루지 않음.
    public void cancelByOwner(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id)
                .filter(w -> w.isOwnedBy(name))
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 대기입니다."));//남의 대기

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

    private ReservationTime findTimeOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 시간입니다."));
    }

    private Theme findThemeOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 테마입니다."));
    }

    // 본인이 이미 예약한 슬롯에는 대기를 신청할 수 없다 (Reservation 상태를 확인해야함 -> 서비스가 조회)
    private void validateNotReservedBySelf(WaitingCreateCommand command) {
        if (reservationRepository.existsBySlotAndName(
                command.getDate(), command.getTimeId(), command.getThemeId(), command.getName())) {
            throw new BusinessRuleViolationException(
                    "이미 본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    //예약이 없는 슬롯에는 대기가 아니라 예약을 해야 한다 (Reservation 상태를 확인행함 ->  서비스가 조회)
    private void validateReservationExists(WaitingCreateCommand command) {
        if (!reservationRepository.existsByDateAndTimeAndTheme(
                command.getDate(), command.getTimeId(), command.getThemeId())) {
            throw new BusinessRuleViolationException(
                    "예약 가능한 시간입니다. 대기가 아닌 예약을 신청해 주세요.");
        }
    }


}

