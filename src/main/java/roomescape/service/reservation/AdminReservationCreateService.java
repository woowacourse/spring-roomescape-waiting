package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.request.ReservationAdminSaveRequest;

@Service
public class AdminReservationCreateService {

    private final ReservationRepository reservationRepository;
    private final ReservationCreateValidator reservationCreateValidator;

    public AdminReservationCreateService(ReservationRepository reservationRepository, ReservationCreateValidator reservationCreateValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationCreateValidator = reservationCreateValidator;
    }

    public Reservation createReservation(ReservationAdminSaveRequest request) {
        ReservationTime reservationTime = reservationCreateValidator.getValidReservationTime(request.timeId());
        reservationCreateValidator.validateDateIsFuture(request.date(), reservationTime);
        Theme theme = reservationCreateValidator.getValidTheme(request.themeId());
        reservationCreateValidator.validateAlreadyBooked(request.date(), request.timeId(), request.themeId());
        Member member = reservationCreateValidator.getValidMember(request.memberId());

        Reservation reservation = request.toEntity(request, reservationTime, theme, member);
        return reservationRepository.save(reservation);
    }
}
