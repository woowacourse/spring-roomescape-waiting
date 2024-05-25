package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationWaitingResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.exception.UnAuthorizedException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.WaitingListExceededException;

@Service
public class ReservationWaitingService {
    private static final long MAX_WAITING_COUNT = 5;

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationService reservationService,
                                     ReservationRepository reservationRepository,
                                     MemberRepository memberRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationWaitingResponse enqueueWaitingList(ReservationRequest request) {
        if (reservationRepository.existsAlreadyWaitingOrBooked(
                request.memberId(), request.themeId(), request.date(), request.timeId())) {
            throw new DuplicatedReservationException(request.themeId(), request.date(), request.timeId());
        }
        Reservation reservation = reservationService.create(request, BookStatus.WAITING);
        long waitingCount = reservationRepository.getWaitingCount(reservation);
        if (waitingCount > MAX_WAITING_COUNT) {
            throw new WaitingListExceededException(reservation.getId());
        }
        return new ReservationWaitingResponse(
                ReservationResponse.from(reservation),
                waitingCount
        );
    }

    @Transactional
    public void cancelWaitingList(long memberId, long id) {
        Reservation reservation = reservationRepository.getById(id);
        Member member = memberRepository.getById(memberId);
        if (reservation.isNotModifiableBy(member)) {
            throw new UnAuthorizedException();
        }
        reservation.cancelWaiting();
        reservationRepository.findFirstWaiting(
                reservation.getTheme(), reservation.getDate(), reservation.getTime()
        ).ifPresent(Reservation::book);
    }
}
