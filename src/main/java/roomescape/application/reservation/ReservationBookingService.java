package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.exception.UnAuthorizedException;
import roomescape.exception.reservation.AlreadyBookedException;

@Service
public class ReservationBookingService {
    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public ReservationBookingService(ReservationService reservationService,
                                     MemberRepository memberRepository,
                                     ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse bookReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())
        ) {
            throw new AlreadyBookedException(request.date(), request.timeId(), request.themeId());
        }
        Reservation reservation = reservationService.create(request, BookStatus.BOOKED);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancelReservation(long memberId, long id) {
        Reservation reservation = reservationRepository.getById(id);
        Member member = memberRepository.getById(memberId);
        if (reservation.isNotModifiableBy(member)) {
            throw new UnAuthorizedException();
        }
        reservation.cancelBooking();
        reservationRepository.findFirstWaiting(
                reservation.getTheme(), reservation.getDate(), reservation.getTime()
        ).ifPresent(Reservation::book);
    }
}
