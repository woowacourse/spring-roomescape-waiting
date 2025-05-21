package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.repository.Users;
import roomescape.business.model.vo.Id;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;
import static roomescape.exception.ErrorCode.USER_NOT_EXIST;
import static roomescape.exception.SecurityErrorCode.AUTHORITY_LACK;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final Users users;
    private final Reservations reservations;
    private final ReservationSlotService slotService;

    @Transactional
    public ReservationDto addAndGet(final LocalDate date, final String timeIdValue, final String themeIdValue, final String userIdValue) {
        val user = users.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        ReservationSlot slot = slotService.findByDateAndTimeIdAndThemeIdOrElseCreate(date, timeIdValue, themeIdValue);

        val reservation = Reservation.create(user, slot);
        reservations.save(reservation);
        return ReservationDto.fromEntity(reservation);
    }

    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        val reservations = this.reservations.findAllWithFilter(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return ReservationDto.fromEntities(reservations);
    }

    public List<ReservationDto> getMyReservations(final String userIdValue) {
        val reservations = this.reservations.findAllByUserId(Id.create(userIdValue));
        return ReservationDto.fromEntities(reservations);
    }

    @Transactional
    public void delete(final String reservationIdValue, final String userIdValue) {
        val reservationId = Id.create(reservationIdValue);
        val reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
        if (!reservation.isSameReserver(userIdValue)) {
            throw new AuthorizationException(AUTHORITY_LACK);
        }
        reservations.deleteById(reservationId);
    }
}
