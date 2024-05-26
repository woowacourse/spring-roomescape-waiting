package roomescape.application.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
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
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.UnAuthorizedException;
import roomescape.exception.reservation.AlreadyBookedException;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.WaitingListExceededException;

@Service
public class ReservationService {
    private static final int MAX_WAITING_COUNT = 5;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              MemberRepository memberRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationResponse bookReservation(ReservationRequest request) {
        if (reservationRepository.existsActiveReservation(
                request.themeId(), request.date(), request.timeId())
        ) {
            throw new AlreadyBookedException(request.date(), request.timeId(), request.themeId());
        }
        Member member = memberRepository.getById(request.memberId());
        Theme theme = themeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservation = new Reservation(member, theme, request.date(), time, now, BookStatus.BOOKED);
        reservationRepository.save(reservation);
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

    @Transactional
    public ReservationWaitingResponse enqueueWaitingList(ReservationRequest request) {
        if (reservationRepository.existsAlreadyWaitingOrBooked(
                request.memberId(), request.themeId(), request.date(), request.timeId())) {
            throw new DuplicatedReservationException(request.themeId(), request.date(), request.timeId());
        }
        Member member = memberRepository.getById(request.memberId());
        Theme theme = themeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        LocalDateTime now = LocalDateTime.now(clock);
        Reservation reservation = reservationRepository.save(
                new Reservation(member, theme, request.date(), time, now, BookStatus.WAITING)
        );

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
    }
}
