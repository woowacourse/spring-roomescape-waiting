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
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.exception.UnAuthorizedException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.WaitingListExceededException;

@Service
public class ReservationWaitingService {
    private static final long MAX_WAITING_COUNT = 5;

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationWaitingService(ReservationService reservationService,
                                     MemberRepository memberRepository,
                                     ReservationStatusRepository reservationStatusRepository) {
        this.reservationService = reservationService;
        this.memberRepository = memberRepository;
        this.reservationStatusRepository = reservationStatusRepository;
    }

    @Transactional
    public ReservationWaitingResponse enqueueWaitingList(ReservationRequest request) {
        Reservation reservation = reservationService.create(request);
        if (reservationStatusRepository.existsAlreadyWaitingOrBooked(reservation)) {
            throw new DuplicatedReservationException(reservation.getId());
        }
        ReservationStatus status = reservationStatusRepository.save(
                new ReservationStatus(reservation, BookStatus.WAITING)
        );
        long waitingCount = reservationStatusRepository.getWaitingCount(status.getReservation());
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
        ReservationStatus reservationStatus = reservationStatusRepository.getById(id);
        Reservation reservation = reservationStatus.getReservation();
        Member member = memberRepository.getById(memberId);
        if (reservation.isNotModifiableBy(member)) {
            throw new UnAuthorizedException();
        }
        reservationStatus.cancelWaiting();
        reservationStatusRepository.findFirstWaiting(reservationStatus.getReservation())
                .ifPresent(ReservationStatus::book);
    }
}
