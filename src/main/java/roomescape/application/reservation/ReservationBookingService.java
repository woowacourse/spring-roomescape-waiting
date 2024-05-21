package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.role.RoleRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class ReservationBookingService {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final RoleRepository roleRepository;

    public ReservationBookingService(ReservationService reservationService,
                                     ReservationRepository reservationRepository,
                                     RoleRepository roleRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public ReservationResponse bookReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())) {
            throw new IllegalArgumentException("이미 존재하는 예약입니다.");
        }
        Reservation reservation = reservationService.create(request);
        return ReservationResponse.from(reservation);
    }


    @Transactional
    public void cancelReservation(long memberId, long id) {
        Reservation reservation = reservationRepository.getById(id);
        if (roleRepository.isAdminByMemberId(memberId) || reservation.isOwnedBy(memberId)) {
            reservationRepository.deleteById(reservation.getId());
            return;
        }
        throw new UnAuthorizedException();
    }
}
