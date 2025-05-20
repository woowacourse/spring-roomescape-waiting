package roomescape.reservation.application;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.MyReservationServiceResponse;
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
import roomescape.reservation.model.vo.ReservationStatus;

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
        try {
            reservationValidator.validateNoDuplication(request.date(), request.timeId(), request.themeId());
            Reservation reservation = Reservation.createFutureReservation(reservationDetails);
            Reservation savedReservation = reservationRepository.save(reservation);
            return ReservationServiceResponse.from(savedReservation);
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }
    }

    public List<MyReservationServiceResponse> getAllByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);
        return reservations.stream()
                .map(this::buildMyReservationServiceResponse)
                .toList();
    }

    private ReservationDetails createReservationDetails(CreateReservationServiceRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        ReservationTheme reservationTheme = reservationThemeRepository.getById(request.themeId());
        Member member = memberRepository.getById(request.memberId());
        return request.toReservationDetails(reservationTime, reservationTheme, member);
    }

    private MyReservationServiceResponse buildMyReservationServiceResponse(Reservation reservation) {
        ReservationStatus reservationStatus = ReservationStatus.getStatus(
                reservation.getReservationDateTime(), LocalDateTime.now()
        );
        return MyReservationServiceResponse.from(reservation, reservationStatus);
    }
}
