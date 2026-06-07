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
        try {
            Long reservationSlotId = getOrCreateReservationSlotId(request);
            reservationSlotRepository.existsByNameAndReservationSlot(reservationSlotId, request.name());
            ReservationSlot reservationSlot = reservationSlotRepository.findById(reservationSlotId);

            Reservation newReservation = reservationSlot.reserve(request.name(), now);
            newReservation = reservationSlotRepository.saveReservation(newReservation);
            int order = reservationSlot.calculateOrder(newReservation);

            return ReservationResponse.from(newReservation, reservationSlot.getSlot(), order);
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION_SLOT);
        }
    }

    @Transactional
    public void update(Long reservationId, LocalDateTime now, ReservationRequest request) {
        try {
            ReservationSlot currentSlot = reservationSlotRepository.findByReservationId(reservationId);

            Long newSlotId = getOrCreateReservationSlotId(request);
            ReservationSlot newSlot = reservationSlotRepository.findById(newSlotId);

            validateSameTheme(currentSlot.getSlot().theme(), newSlot.getSlot().theme());

            Reservation prevReservation = currentSlot.deleteReservation(reservationId, now);
            Reservation updatedReservation = newSlot.updateReservation(prevReservation, now);

            reservationSlotRepository.updateReservation(prevReservation);
            reservationSlotRepository.updateReservation(updatedReservation);
            reservationSlotRepository.updateReservation(currentSlot.getReservedReservation());
            reservationSlotRepository.updateReservation(currentSlot.getReservedReservation());


        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    public void delete(LocalDateTime now, Long reservationId, String name) {
        ReservationSlot currentSlot = reservationSlotRepository.findByReservationId(reservationId);
        Reservation deletedReservation = currentSlot.deleteReservation(reservationId, now);
        validateReservationOwner(deletedReservation, name);
        reservationSlotRepository.updateReservation(deletedReservation);
        reservationSlotRepository.updateReservation(currentSlot.getReservedReservation());
    }

    public List<ReservationResponse> findAllByName(String username) {
        return reservationQueryRepository.findByUserName(username);
    }

    private Long getOrCreateReservationSlotId(ReservationRequest request) {
        try {
            Optional<Long> reservationSlotId = reservationSlotRepository.findIdByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
            return reservationSlotId.orElseGet(() -> reservationSlotRepository.save(request.date(), request.timeId(), request.themeId()));
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateSameTheme(Theme reservationTheme, Theme newReservationTheme) {
        if (!reservationTheme.equals(newReservationTheme)) {
            throw new CustomException(ErrorCode.UNALLOWED_CHANGE_RESERVATION_THEME);
        }
    }

    private void validateReservationOwner(Reservation reservation, String name) {
        if (!reservation.getName().equals(name)) {
            throw new CustomException(ErrorCode.COMMON_UNAUTHORIZED);
        }
    }
}
