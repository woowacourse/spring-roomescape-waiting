package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.exception.ReservationException;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.service.ReservationValidator;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.global.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationValidator reservationValidator;

    public ReservationServiceResponse create(CreateReservationServiceRequest request) {
        ReservationDetails reservationDetails = createReservationDetails(request);
        Member member = memberRepository.getById(request.memberId());
        try {
            reservationValidator.validateNoDuplication(request.date(), request.timeId(), request.themeId());
            Reservation reservation = Reservation.createFutureReservation(reservationDetails);
            Reservation savedReservation = reservationRepository.save(reservation);
            return ReservationServiceResponse.from(savedReservation, member.getName());
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }
    }

    private ReservationDetails createReservationDetails(CreateReservationServiceRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        ReservationTheme reservationTheme = reservationThemeRepository.getById(request.themeId());
        return request.toReservationDetails(reservationTime, reservationTheme);
    }

}
