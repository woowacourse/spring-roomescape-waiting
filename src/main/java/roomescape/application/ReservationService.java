package roomescape.application;

import java.time.LocalDate;
import java.util.List;
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

@Service
public class ReservationService {

    private final ReservationFactory reservationFactory;
    private final ReservationCommandRepository reservationCommandRepository;
    private final ReservationQueryRepository reservationQueryRepository;

    public ReservationService(ReservationFactory reservationFactory,
                              ReservationCommandRepository reservationCommandRepository,
                              ReservationQueryRepository reservationQueryRepository) {
        this.reservationCommandRepository = reservationCommandRepository;
        this.reservationQueryRepository = reservationQueryRepository;
        this.reservationFactory = reservationFactory;
    }

    @Transactional
    public ReservationResponse create(LoginMember member, ReservationRequest request) {
        Reservation reservation = reservationFactory.create(member.id(), request);
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
        return reservationQueryRepository.findByCriteria(themeId, memberId, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        return reservationQueryRepository.findAllByMemberIdOrderByDateDesc(memberId).stream()
                .map(reservation -> new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                        reservation.getDate(), reservation.getTime().getStartAt(), "예약"))
                .toList();
    }
}
