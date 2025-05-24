package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.service.dto.request.FilteringReservationRequest;
import roomescape.reservation.service.dto.response.BookedReservationTimeResponse;
import roomescape.reservation.service.dto.response.MyReservationsResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void delete(final Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
        reservationRepository.deleteById(id);
        handleWaiting(reservation);
    }

    private void handleWaiting(Reservation reservation) {
        Waiting firstWaiting = waitingRepository.findFirstByReservationInfo(
                reservation.getDate(), reservation.getTime(), reservation.getTheme()
        );
        if (firstWaiting != null) {
            waitingRepository.delete(firstWaiting);
            reservationRepository.save(Reservation.of(firstWaiting));
        }
    }

    public List<BookedReservationTimeResponse> getAvailableTimes(final LocalDate date, final Long themeId) {
        // TODO: repository에서 처리
        // return reservationTimeRepository.findAllWithBooked(date, themeId);

        Map<ReservationTime, Boolean> allTimes = processAlreadyBookedTimesMap(date, themeId);

        return allTimes.entrySet()
                .stream()
                .map(entry -> bookedReservationTimeResponseOf(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<ReservationTime, Boolean> processAlreadyBookedTimesMap(final LocalDate date, final Long themeId) {
        Set<ReservationTime> alreadyBookedTimes = getAlreadyBookedTimes(date, themeId);

        return reservationTimeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Function.identity(), alreadyBookedTimes::contains));
    }

    private BookedReservationTimeResponse bookedReservationTimeResponseOf(
            final ReservationTime reservationTime,
            final boolean isAlreadyBooked
    ) {
        return new BookedReservationTimeResponse(ReservationTimeResponse.from(reservationTime), isAlreadyBooked);
    }

    private Set<ReservationTime> getAlreadyBookedTimes(final LocalDate date, final Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId)
                .stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
    }
}
