package roomescape.service;

import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.WaitingReservationResponse;
import roomescape.domain.ThemeSlot;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeSlotRepository;
import roomescape.repository.WaitingRepository;

import static roomescape.global.exception.ErrorCode.RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeSlotRepository themeSlotRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            ThemeSlotRepository themeSlotRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.themeSlotRepository = themeSlotRepository;
    }

    @Transactional
    public WaitingReservationResponse saveWaiting(String memberName, Long themeSlotId) {
        ThemeSlot themeSlot = getThemeSlotForUpdateOrElseThrow(themeSlotId);
        validateAlreadyReserved(themeSlotId);
        validateDuplicatedWaiting(memberName, themeSlot);

        Waiting savedWaiting = waitingRepository.save(new Waiting(memberName, themeSlot));
        WaitingWithRank waitingWithRank = waitingRepository.findWithRankByMemberName(memberName)
                .stream()
                .filter(it -> Objects.equals(it.waiting().getId(), savedWaiting.getId()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.WAITING_NOT_FOUND));
        return WaitingReservationResponse.from(waitingWithRank);
    }

    @Transactional
    public void deleteWaiting(Long waitingId, String memberName) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ErrorCode.WAITING_NOT_FOUND));
        if (!waiting.isOwnedBy(memberName)) {
            throw new CustomException(ErrorCode.WAITING_NOT_ALLOWED);
        }
        waitingRepository.delete(waiting);
    }

    private void validateAlreadyReserved(Long themeSlotId) {
        if (!reservationRepository.existsConfirmedByThemeSlotId(themeSlotId)) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }
    }

    private void validateDuplicatedWaiting(String memberName, ThemeSlot themeSlot) {
        boolean hasReservation = reservationRepository.existsByThemeSlotIdAndMemberName(memberName, themeSlot.getId());
        boolean hasWaiting = waitingRepository.existsByMemberNameAndThemeAndDateAndTime(
                memberName,
                themeSlot.getTheme(),
                themeSlot.getDate(),
                themeSlot.getTime()
        );
        if (hasReservation || hasWaiting) {
            throw new CustomException(RESERVATION_ALREADY_EXIST_BY_USER_AND_SLOT);
        }
    }

    @NonNull
    private ThemeSlot getThemeSlotForUpdateOrElseThrow(Long themeSlotId) {
        return themeSlotRepository.findByIdForUpdate(themeSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_SLOT_NOT_FOUND));
    }
}
