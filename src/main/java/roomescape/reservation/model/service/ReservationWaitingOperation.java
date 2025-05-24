package roomescape.reservation.model.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.dto.ReservationWaitingDetails;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;

@Component
@RequiredArgsConstructor
public class ReservationWaitingOperation {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationValidator reservationValidator;
    private final ReservationWaitingValidator reservationWaitingValidator;

    public void waiting(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationWaitingDetails reservationWaitingDetails = createReservationWaitingDetails(date, timeId, themeId,
                memberId);
        reservationValidator.validateExistence(date, timeId, themeId);
        reservationWaitingValidator.validateAlreadyWaiting(date, timeId, themeId, memberId);
        ReservationWaiting reservationWaiting = ReservationWaiting.createFuture(reservationWaitingDetails);
        reservationWaitingRepository.save(reservationWaiting);
    }

    private ReservationWaitingDetails createReservationWaitingDetails(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        ReservationTheme reservationTheme = reservationThemeRepository.getById(themeId);
        Member member = memberRepository.getById(memberId);
        return ReservationWaitingDetails.builder()
                .date(date)
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
