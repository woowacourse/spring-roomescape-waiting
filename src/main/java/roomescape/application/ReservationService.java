package roomescape.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.LoginMember;
import roomescape.application.dto.MyReservationResponse;
import roomescape.application.dto.ReservationCriteria;
import roomescape.application.dto.ReservationRequest;
import roomescape.application.dto.ReservationResponse;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationFactory;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.WaitingQueryRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationFactory reservationFactory;
    private final ReservationCommandRepository reservationCommandRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final WaitingQueryRepository waitingQueryRepository;

    public ReservationService(ReservationFactory reservationFactory,
                              ReservationCommandRepository reservationCommandRepository,
                              ReservationQueryRepository reservationQueryRepository,
                              WaitingQueryRepository waitingQueryRepository) {
        this.reservationFactory = reservationFactory;
        this.reservationCommandRepository = reservationCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.waitingQueryRepository = waitingQueryRepository;
    }

    @Transactional
    public ReservationResponse create(LoginMember loginMember, ReservationRequest request) {
        Reservation reservation = reservationFactory.create(loginMember.id(), request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservationCommandRepository.save(reservation));
    }

    @Transactional
    public void deleteById(Long id) {
        Reservation reservation = reservationQueryRepository.getById(id);
        reservationCommandRepository.delete(reservation);
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationQueryRepository.findAll();
        return convertToReservationResponses(reservations);
    }

    private List<ReservationResponse> convertToReservationResponses(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByCriteria(ReservationCriteria reservationCriteria) {
        Long themeId = reservationCriteria.themeId();
        Long memberId = reservationCriteria.memberId();
        LocalDate dateFrom = reservationCriteria.dateFrom();
        LocalDate dateTo = reservationCriteria.dateTo();
        return reservationQueryRepository.findByCriteria(memberId, dateFrom, dateTo, themeId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservationsAndWaiting(Long memberId) {
        Stream<MyReservationResponse> reservationsStream = reservationQueryRepository.findAllByMemberIdOrderByDateDesc(memberId).stream()
                .map(MyReservationResponse::convert);

        Stream<MyReservationResponse> waitingsStream = waitingQueryRepository.findWaitingWithRankByMemberId(memberId).stream()
                .map(MyReservationResponse::convert);

        return Stream.concat(reservationsStream, waitingsStream)
                .sorted(Comparator.comparing(MyReservationResponse::date).reversed())
                .toList();
    }
}
