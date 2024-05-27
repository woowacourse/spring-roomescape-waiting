package roomescape.service.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.dto.ReservationResponse;
import roomescape.domain.dto.ReservationWaitingResponse;
import roomescape.domain.dto.ReservationsMineResponse;
import roomescape.domain.dto.ResponsesWrapper;
import roomescape.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationSearchService {
    private final ReservationRepository reservationRepository;

    public ReservationSearchService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationResponse> findEntireReservations() {
        final List<ReservationResponse> reservationResponses = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservationResponses);
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationWaitingResponse> findEntireWaitingReservations() {
        final List<ReservationWaitingResponse> reservations = reservationRepository.findByStatus(ReservationStatus.WAITING)
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservations);
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationResponse> findReservations(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                                                  final LocalDate dateTo) {
        final List<ReservationResponse> reservationResponses = reservationRepository
                .findAllByTheme_IdAndMember_IdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
        return new ResponsesWrapper<>(reservationResponses);
    }

    @Transactional(readOnly = true)
    public ResponsesWrapper<ReservationsMineResponse> findMemberReservations(final Member member) {
        final List<ReservationsMineResponse> reservationsMineResponses = reservationRepository.findByMember(member)
                .stream()
                .map(reservation -> buildReservationMineResponse(reservation, member))
                .toList();
        return new ResponsesWrapper<>(reservationsMineResponses);
    }

    private ReservationsMineResponse buildReservationMineResponse(final Reservation reservation, final Member member) {
        if (reservation.isWaiting()) {
            return ReservationsMineResponse.from(reservation, calculateWaitingNumber(reservation, member));
        }
        return ReservationsMineResponse.from(reservation, 0);
    }

    private Integer calculateWaitingNumber(final Reservation reservation, final Member member) {
        final List<Reservation> reservations = reservationRepository.findByDateAndTime_IdAndTheme_Id(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        return Math.toIntExact(reservations.stream()
                .takeWhile(found -> !found.getMemberId().equals(member.getId()))
                .count());
    }
}
