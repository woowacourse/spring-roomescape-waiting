package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.request.FilteringReservationRequest;
import roomescape.reservation.service.dto.response.BookedReservationTimeResponse;
import roomescape.reservation.service.dto.response.MyReservationsResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationsResponse> getAllMemberReservations(LoginMember loginMember) {
        ArrayList<MyReservationsResponse> results = new ArrayList<>(
                reservationRepository.findAllByMemberId(loginMember.id())
                        .stream()
                        .map(MyReservationsResponse::from)
                        .toList()
        );
        results.addAll(
                waitingRepository.findAllWaitingInfoByMemberId(loginMember.id())
                        .stream()
                        .map(MyReservationsResponse::from)
                        .toList()
        );
        return Collections.unmodifiableList(results);
    }

    public List<ReservationResponse> findReservationByFiltering(final FilteringReservationRequest request) {
        Long themeId = request.themeId();
        Long memberId = request.memberId();
        LocalDate dateFrom = request.dateFrom();
        LocalDate dateTo = request.dateTo();

        return reservationRepository.findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void delete(final Long id, LoginMember loginMember) {
        Reservation reservation = getAuthorizedReservation(id, loginMember);
        reservationRepository.deleteById(id);
        promoteFirstWaitingToReservation(reservation);
    }

    private Reservation getAuthorizedReservation(final Long id, LoginMember loginMember) {
        return switch (loginMember.role()) {
            case MEMBER -> reservationRepository.findByIdAndMemberId(id, loginMember.id())
                    .orElseThrow(() -> new ForbiddenException("삭제 권한이 없습니다."));
            case ADMIN -> reservationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        };
    }

    private void promoteFirstWaitingToReservation(Reservation reservation) {
        Waiting firstWaiting = waitingRepository.findFirstByReservationInfo(
                reservation.getDate(), reservation.getTime(), reservation.getTheme()
        );
        if (firstWaiting != null) {
            waitingRepository.delete(firstWaiting);
            reservationRepository.save(Reservation.of(firstWaiting));
        }
    }

    public List<BookedReservationTimeResponse> getAvailableTimes(final LocalDate date, final Long themeId) {
         return reservationTimeRepository.findAllWithBooked(date, themeId)
                 .stream()
                 .map(BookedReservationTimeResponse::from)
                 .toList();
    }
}
