package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ReservationSaveRequest;

@Service
public class ReservationCreateService {

    private final ReservationCreateValidator reservationCreateValidator;
    private final ReservationRepository reservationRepository;

    public ReservationCreateService(ReservationCreateValidator reservationCreateValidator,
                                    ReservationRepository reservationRepository) {
        this.reservationCreateValidator = reservationCreateValidator;
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(ReservationSaveRequest request, Member member, ReservationStatus reservationStatus) {
        ReservationTime reservationTime = reservationCreateValidator.getValidReservationTime(request.timeId());
        reservationCreateValidator.validateDateIsFuture(request.date(), reservationTime);
        Theme theme = reservationCreateValidator.getValidTheme(request.themeId());
        reservationCreateValidator.validateAlreadyBooked(request.date(), request.timeId(), request.themeId(), reservationStatus);
        reservationCreateValidator.validateOwnReservationExist(member, theme, reservationTime, request.date());

        Reservation reservation = request.toEntity(reservationTime, theme, member, reservationStatus);

        return reservationRepository.save(reservation);
    }
}
