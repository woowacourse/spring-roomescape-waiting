package roomescape.service.reservationwaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationWaitingService(
            final ReservationRepository reservationRepository,
            final ReservationWaitingRepository reservationWaitingRepository,
            final ReservationSlotRepository reservationSlotRepository,
            final ThemeService themeService,
            final ReservationTimeService reservationTimeService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    @Transactional
    public ReservationWaiting save(
            final String name,
            final LocalDate date,
            final long themeId,
            final long timeId,
            final LocalDateTime requestedAt
    ) {
        ReservationName waitingName = ReservationName.from(name);
        ReservationSlot slot = new ReservationSlot(
                date,
                themeService.getById(themeId),
                reservationTimeService.getById(timeId)
        );
        ReservationSlot savedSlot = findExistingSlot(slot);
        Reservation reservation = reservationRepository.findBySlot(savedSlot)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));

        validateWaitableName(savedSlot, reservation, waitingName);

        ReservationWaiting nonIdReservationWaiting = createNewWaiting(savedSlot, waitingName, requestedAt);
        try {
            return reservationWaitingRepository.save(nonIdReservationWaiting);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationWaiting> getAll() {
        return reservationWaitingRepository.findAll();
    }

    private ReservationSlot findExistingSlot(final ReservationSlot slot) {
        return reservationSlotRepository.findBySlot(slot)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));
    }

    private void validateWaitableName(
            final ReservationSlot slot,
            final Reservation reservation,
            final ReservationName waitingName
    ) {
        if (reservation.getName().equals(waitingName.value())) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 예약한 사람은 같은 예약에 대기할 수 없습니다."
            );
        }

        ReservationWaitingLine waitingLine = reservationWaitingRepository.findLineBySlot(slot);

        if (waitingLine.containsName(waitingName)) {
            throw new ConflictException(
                    ErrorCode.RESERVATION_WAITING_DUPLICATED,
                    "이미 같은 예약에 대기 중입니다."
            );
        }
    }

    private ReservationWaiting createNewWaiting(
            final ReservationSlot slot,
            final ReservationName waitingName,
            final LocalDateTime requestedAt
    ) {
        try {
            return ReservationWaiting.createNew(slot, waitingName.value(), requestedAt);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, exception.getMessage());
        }
    }

    @Transactional
    public void deleteByIdAndName(final Long waitingId, final String name) {
        ReservationName waitingName = ReservationName.from(name);
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(waitingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                        "삭제된 대기 데이터가 없습니다."
                ));

        if (!reservationWaiting.hasName(waitingName.value())) {
            throw new ResourceNotFoundException(
                    ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                    "삭제된 대기 데이터가 없습니다."
            );
        }

        reservationWaitingRepository.delete(reservationWaiting);
    }

    @Transactional
    public void deleteById(final Long waitingId) {
        ReservationWaiting reservationWaiting = reservationWaitingRepository.findById(waitingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                        "삭제된 대기 데이터가 없습니다."
                ));

        reservationWaitingRepository.delete(reservationWaiting);
    }
}
