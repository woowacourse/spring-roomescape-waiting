package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.application.dto.request.CreateReservationWaitingServiceRequest;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.exception.ReservationException;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;
import roomescape.reservation.model.service.ReservationValidator;

@Service
@RequiredArgsConstructor
public class UserReservationWaitingService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationValidator reservationValidator;

    public void create(CreateReservationWaitingServiceRequest request) {
        ReservationWaitingDetails reservationWaitingDetails = createReservationWaitingDetails(request);
        try {
            reservationValidator.validateExistence(request.date(), request.timeId(), request.themeId());
            ReservationWaiting reservationWaiting = ReservationWaiting.createFuture(reservationWaitingDetails);
            reservationWaitingRepository.save(reservationWaiting);
        } catch (ReservationException e) {
            throw new BusinessRuleViolationException(e.getMessage(), e);
        }
    }

    private ReservationWaitingDetails createReservationWaitingDetails(CreateReservationWaitingServiceRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        ReservationTheme reservationTheme = reservationThemeRepository.getById(request.themeId());
        Member member = memberRepository.getById(request.memberId());
        return request.toReservationWaitingDetails(reservationTime, reservationTheme, member);
    }
}
