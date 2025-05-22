package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.business.reader.ReservationSlotReader;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;

import static roomescape.exception.ErrorCode.RESERVATION_DUPLICATED;
import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;
import static roomescape.exception.ErrorCode.USER_NOT_EXIST;
import static roomescape.exception.SecurityErrorCode.AUTHORITY_LACK;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final Users users;
    private final Reservations reservations;
    private final ReservationSlotService slotService;
    private final ReservationSlotReader slotReader;

    public ReservationDto addAndGet(final LocalDate date, final String timeIdValue, final String themeIdValue, final String userIdValue) {
        User user = users.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));

        ReservationSlot slot = slotReader.findByDateAndTimeIdAndThemeId(date, timeIdValue, themeIdValue)
                .orElseGet(() -> slotService.addAndGet(date, timeIdValue, themeIdValue));

        if (!reservations.isSlotFreeFor(slot, user)) {
            throw new DuplicatedException(RESERVATION_DUPLICATED);
        }

        Reservation reservation = new Reservation(user, slot);
        reservations.save(reservation);
        return ReservationDto.fromEntity(reservation);
    }

    public void delete(final String reservationIdValue, final String userIdValue) {
        Id reservationId = Id.create(reservationIdValue);
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
        if (!reservation.isSameReserver(userIdValue)) {
            throw new AuthorizationException(AUTHORITY_LACK);
        }
        reservations.deleteById(reservationId);
    }
}
