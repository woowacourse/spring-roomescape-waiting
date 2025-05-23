package roomescape.reservation.model.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.model.dto.ReservationDetails;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.ReservationWaitingRepository;

@Component
@RequiredArgsConstructor
public class ReservationOperation {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationValidator reservationValidator;

    public Reservation reserve(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationDetails reservationDetails = createReservationDetails(date, timeId, themeId, memberId);
        reservationValidator.validateNoDuplication(date, timeId, themeId);
        return Reservation.createFuture(reservationDetails);
    }

    public void cancel(Reservation reservation) {
        reservationRepository.remove(reservation);
        reservationWaitingRepository.findFirstByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        ).ifPresent(reservationWaiting -> reservationRepository.save(Reservation.fromWaiting(reservationWaiting)));
    }

    private ReservationDetails createReservationDetails(LocalDate date, Long timeId, Long themeId, Long memberId) {
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        ReservationTheme reservationTheme = reservationThemeRepository.getById(themeId);
        Member member = memberRepository.getById(memberId);

        return ReservationDetails.builder()
                .date(date)
                .reservationTime(reservationTime)
                .reservationTheme(reservationTheme)
                .member(member)
                .build();
    }
}
