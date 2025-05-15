package roomescape.business.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.repository.ReservationTimeRepository;
import roomescape.business.model.repository.ThemeRepository;
import roomescape.business.model.repository.UserRepository;
import roomescape.business.model.vo.Id;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.business.DuplicatedException;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVATION_DUPLICATED;
import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;
import static roomescape.exception.ErrorCode.THEME_NOT_EXIST;
import static roomescape.exception.ErrorCode.USER_NOT_EXIST;
import static roomescape.exception.SecurityErrorCode.AUTHORITY_LACK;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationDto addAndGet(final LocalDate date, final String timeIdValue, final String themeIdValue, final String userIdValue) {
        User user = userRepository.findById(Id.create(userIdValue))
                .orElseThrow(() -> new NotFoundException(USER_NOT_EXIST));
        ReservationTime reservationTime = reservationTimeRepository.findById(Id.create(timeIdValue))
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
        Theme theme = themeRepository.findById(Id.create(themeIdValue))
                .orElseThrow(() -> new NotFoundException(THEME_NOT_EXIST));

        if (reservationRepository.isDuplicateDateAndTimeAndTheme(date, reservationTime.startTimeValue(), theme.getId())) {
            throw new DuplicatedException(RESERVATION_DUPLICATED);
        }

        Reservation reservation = Reservation.create(user, date, reservationTime, theme);
        reservationRepository.save(reservation);
        return ReservationDto.fromEntity(reservation);
    }

    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        List<Reservation> reservations = reservationRepository.findAllWithFilter(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return ReservationDto.fromEntities(reservations);
    }

    public void delete(final String reservationIdValue, final String userIdValue) {
        Id reservationId = Id.create(reservationIdValue);
        final Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
        if (!reservation.isSameReserver(userIdValue)) {
            throw new AuthorizationException(AUTHORITY_LACK);
        }
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationDto> getMyReservations(final String userIdValue) {
        Id userId = Id.create(userIdValue);
        List<Reservation> myReservations = reservationRepository.findAllWithFilter(null, userId, null, null);
        return ReservationDto.fromEntities(myReservations);
    }
}
