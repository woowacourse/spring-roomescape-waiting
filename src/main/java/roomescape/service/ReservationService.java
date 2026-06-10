package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Service
public class ReservationService {
    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    public ReservationService(ReservationSlotRepository reservationSlotRepository, ReservationQueryRepository reservationQueryRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
        this.reservationQueryRepository = reservationQueryRepository;
    }

    @Transactional
    public ReservationResponse save(LocalDateTime now, ReservationRequest request) {
        Long reservationSlotId = getOrCreateReservationSlotId(request);

        ReservationSlot reservationSlot = reservationSlotRepository.findByIdForUpdate(reservationSlotId);
        Reservation newReservation = reservationSlot.reserve(request.name(), now);
        int order = reservationSlot.calculateOrder(newReservation);
        newReservation = reservationSlotRepository.saveReservation(newReservation);
        return ReservationResponse.from(newReservation, reservationSlot.getSlot(), order);
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        ReservationSlot currentSlot = reservationSlotRepository.findByReservationId(reservationId);

        Long newSlotId = getOrCreateReservationSlotId(request);
        ReservationSlot newSlot = reservationSlotRepository.findById(newSlotId);

        validateSameTheme(currentSlot.getSlot().theme(), newSlot.getSlot().theme());

        Reservation movedReservation = currentSlot.moveOut(reservationId, request.name(), now);
        Reservation updatedReservation = newSlot.moveIn(movedReservation, request.name(), now);

        reservationSlotRepository.updateReservation(updatedReservation);
        currentSlot.getReservedReservation()
                .ifPresent(reservationSlotRepository::updateReservation);
        newSlot.getReservedReservation()
                .ifPresent(reservationSlotRepository::updateReservation);
    }

    public void delete(LocalDateTime now, Long reservationId, String name) {
        ReservationSlot currentSlot = reservationSlotRepository.findByReservationId(reservationId);
        Reservation deletedReservation = currentSlot.deleteReservation(reservationId, name, now);
        reservationSlotRepository.updateReservation(deletedReservation);
        currentSlot.getReservedReservation()
                .ifPresent(reservationSlotRepository::updateReservation);
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationQueryRepository.findByUserName(username);
    }

    private Long getOrCreateReservationSlotId(ReservationRequest request) {
        try {
            Optional<Long> reservationSlotId = reservationSlotRepository.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
            return reservationSlotId.orElseGet(() ->
                    reservationSlotRepository.save(request.date(), request.timeId(), request.themeId())
            );
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION_SLOT);
        }
    }

    private void validateSameTheme(Theme reservationTheme, Theme newReservationTheme) {
        if (!reservationTheme.equals(newReservationTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }
}
