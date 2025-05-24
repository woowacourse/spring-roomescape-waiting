package roomescape.reservation.model.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public Reservation reserve(LocalDate date, Long timeId, Long themeId, Long memberId) {
        reservationValidator.validateNoDuplication(date, timeId, themeId);
        ReservationDetails reservationDetails = createReservationDetails(date, timeId, themeId, memberId);
        Reservation reservation = Reservation.createFuture(reservationDetails);
        Reservation savedReservation = reservationRepository.save(reservation);
        return savedReservation;
    }

    @Transactional
    public void cancel(Reservation reservation) {
        reservation.changeToCancel();
        reservationWaitingRepository.findFirstByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()
        ).ifPresent(reservationWaiting -> {
            reservationWaiting.changeToAccept();
            Reservation newReservation = Reservation.confirmedFromWaiting(reservationWaiting);
            reservationRepository.save(newReservation);
        });
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
