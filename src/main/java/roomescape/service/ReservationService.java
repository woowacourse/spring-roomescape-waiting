package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.domain.vo.LockedReservationSlots;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Service
public class ReservationService {
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationSlotRepository reservationSlotRepository, ReservationRepository reservationRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Long reservationSlotId = getOrCreateReservationSlotId(request);
        ReservationSlot reservationSlot = reservationSlotRepository.findByIdForUpdate(reservationSlotId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
        Reservation newReservation = reservationSlot.reserve(request.name(), now);
        reservationSlotRepository.flush();

        int order = reservationSlot.calculateOrder(newReservation);
        return ReservationResponse.from(newReservation, reservationSlot, order);
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        Long currentSlotId = reservationSlotRepository.findSlotIdByReservationId(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
        Long newSlotId = getOrCreateReservationSlotId(request);

        LockedReservationSlots lockedSlots = findBothSlotsForUpdate(currentSlotId, newSlotId);

        ReservationSlot currentSlot = lockedSlots.currentSlot();
        ReservationSlot newSlot = lockedSlots.newSlot();

        validateSameTheme(currentSlot.getTheme().getName(), newSlot.getTheme().getName());

        Reservation reservation = currentSlot.moveOut(reservationId, request.name(), now);
        newSlot.moveIn(reservation, request.name(), now);
    }

    @Transactional
    public void delete(LocalDateTime now, Long reservationId, String name) {
        ReservationSlot currentSlot = reservationSlotRepository.findByReservationIdForUpdate(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
        currentSlot.deleteReservation(reservationId, name, now);
        reservationSlotRepository.flush();
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationRepository.findByUserName(username);
    }

    private Long getOrCreateReservationSlotId(ReservationRequest request) {
        Optional<Long> reservationSlotId = reservationSlotRepository.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        return reservationSlotId
                .orElseGet(() -> saveReservationSlot(request));
    }

    private Long saveReservationSlot(ReservationRequest request) {
        try {
            return reservationSlotRepository.save(
                    request.date(),
                    request.timeId(),
                    request.themeId()
            );
        } catch (DataIntegrityViolationException e) {
            return reservationSlotRepository.findIdByDateAndTimeIdAndThemeId(
                            request.date(),
                            request.timeId(),
                            request.themeId()
                    )
                    .orElseThrow(() -> new CustomException(ErrorCode.DUPLICATE_RESERVATION_SLOT));
        }
    }

    private void validateSameTheme(String reservationThemeName, String newReservationThemeName) {
        if (!reservationThemeName.equals(newReservationThemeName)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }

    private LockedReservationSlots findBothSlotsForUpdate(Long currentSlotId, Long newSlotId) {
        long firstLockId = Math.min(currentSlotId, newSlotId);
        long secondLockId = Math.max(currentSlotId, newSlotId);

        ReservationSlot first = reservationSlotRepository.findByIdForUpdate(firstLockId).orElseThrow();
        ReservationSlot second = reservationSlotRepository.findByIdForUpdate(secondLockId).orElseThrow();

        if (currentSlotId.equals(firstLockId)) {
            return new LockedReservationSlots(first, second);
        }

        return new LockedReservationSlots(second, first);
    }
}
