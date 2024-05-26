package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.domain.Date;
import roomescape.reservation.domain.FilterInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationAddRequest;
import roomescape.reservation.dto.ReservationFilterRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.reservation.exception.model.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.time.domain.Time;
import roomescape.time.exception.TimeExceptionCode;
import roomescape.waiting.domain.Waiting;

@Service
public class ReservationService {

    private static final int MAX_WAITING_COUNT = 5;
    public static final int NUMBER_OF_ONE_WEEK = 7;
    public static final int TOP_THEMES_LIMIT = 10;

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation addReservation(ReservationAddRequest reservationAddRequest) {
        Reservation reservation = Reservation.of(reservationAddRequest.date(), reservationAddRequest.time(),
                reservationAddRequest.theme(), reservationAddRequest.member(), ReservationStatus.RESERVED);

        return reservationRepository.save(reservation);
    }

    public Reservation addWaitingReservation(ReservationAddRequest reservationAddRequest, long memberId) {
        validateDuplicateReservation(reservationAddRequest, memberId);
        validateIsOverMaxWaitingCount(reservationAddRequest);

        Reservation reservation = Reservation.of(reservationAddRequest.date(), reservationAddRequest.time(),
                reservationAddRequest.theme(), reservationAddRequest.member(), ReservationStatus.WAITING);

        return reservationRepository.save(reservation);
    }

    public void addAdminReservation(ReservationAddRequest reservationAddRequest) {
        Reservation saveReservation = Reservation.of(reservationAddRequest.date(), reservationAddRequest.time(),
                reservationAddRequest.theme(), reservationAddRequest.member(), ReservationStatus.RESERVED);

        ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }


    public List<Reservation> findReservationsOrderByDateAndTime() {
        return reservationRepository.findAllByOrderByDateAscTimeAsc();
    }

    public List<Time> findBookedTimes(long themeId, LocalDate date) {
        Date findDate = Date.dateFrom(date);
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndDate(themeId, findDate);

        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    public List<Reservation> findFilteredReservations(ReservationFilterRequest reservationFilterRequest) {
        FilterInfo filterInfo = reservationFilterRequest.toFilterInfo();

        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(filterInfo.getMemberId(),
                filterInfo.getThemeId(), filterInfo.getFromDate(), filterInfo.getToDate());
    }

    public List<Reservation> findStatusReservations(long memberId, ReservationStatus reservationStatus) {
        return reservationRepository.findAllByMemberIdAndReservationStatus(memberId, reservationStatus);
    }

    public List<Reservation> findWaitings() {
        return reservationRepository.findByReservationStatus(ReservationStatus.WAITING);
    }

    public List<Waiting> findWaitingWithRank(long memberId) {
        List<Reservation> waitingReservations = reservationRepository.findAllByMemberIdAndReservationStatus(memberId,
                ReservationStatus.WAITING);

        List<Waiting> waitings = new ArrayList<>();

        for (Reservation reservation : waitingReservations) {
            int rank = reservationRepository.countByThemeAndDateAndTimeAndIdLessThan(reservation.getTheme(),
                    Date.dateFrom(reservation.getDate()), reservation.getReservationTime(), reservation.getId());

            waitings.add(new Waiting(reservation, rank));
        }
        return waitings;
    }

    public List<Theme> findRankedThemes(LocalDate today) {
        LocalDate beforeOneWeek = today.minusDays(NUMBER_OF_ONE_WEEK);

        return reservationRepository.findAllByDateOrderByThemeIdCountLimit(beforeOneWeek, today,
                TOP_THEMES_LIMIT);
    }

    @Transactional
    public void removeReservation(long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.isAfterCancelDate(LocalDate.now())) {
            throw new RoomEscapeException(ReservationExceptionCode.CAN_NOT_CANCEL_AFTER_MIN_CANCEL_DATE);
        }

        reservation.setReservationStatus(ReservationStatus.CANCEL);
        waitingToReservation(reservation);
    }

    public void removeWaitingReservations(long waitingId) {
        reservationRepository.deleteById(waitingId);
    }

    public void validateBeforeRemoveTheme(long themeId) {
        List<Reservation> reservation = reservationRepository.findByThemeId(themeId);

        if (!reservation.isEmpty()) {
            throw new RoomEscapeException(ThemeExceptionCode.USING_THEME_RESERVATION_EXIST);
        }
    }

    public void validateReservationExistence(long timeId) {
        List<Reservation> reservation = reservationRepository.findByTimeId(timeId);

        if (!reservation.isEmpty()) {
            throw new RoomEscapeException(TimeExceptionCode.EXIST_RESERVATION_AT_CHOOSE_TIME);
        }
    }

    public void waitingToReservation(Reservation reservation) {
        Optional<Reservation> topWaiting = reservationRepository.findFirstByDateAndThemeAndTimeAndReservationStatus(
                Date.dateFrom(reservation.getDate()), reservation.getTheme(), reservation.getReservationTime(),
                ReservationStatus.WAITING);

        if (topWaiting.isPresent()) {
            Reservation nextReservation = topWaiting.get();
            nextReservation.setReservationStatus(ReservationStatus.RESERVED);
        }
    }

    private void validateIsOverMaxWaitingCount(ReservationAddRequest reservationRequest) {
        int countWaiting = reservationRepository.countByThemeIdAndDateAndTimeIdAndReservationStatus(
                reservationRequest.theme().getId(), Date.dateFrom(reservationRequest.date()),
                reservationRequest.time().getId(), ReservationStatus.WAITING);

        if (countWaiting >= MAX_WAITING_COUNT) {
            throw new RoomEscapeException(ReservationExceptionCode.WAITING_IS_MAX);
        }
    }

    private void validateDuplicateReservation(ReservationAddRequest reservationRequest, long memberId) {
        Optional<Reservation> duplicateReservation = reservationRepository.findByDateAndMemberIdAndThemeIdAndTimeId(
                Date.saveFrom(reservationRequest.date()), memberId, reservationRequest.theme().getId(),
                reservationRequest.time().getId());

        if (duplicateReservation.isPresent()) {
            throw new RoomEscapeException(ReservationExceptionCode.DUPLICATE_RESERVATION);
        }
    }
}
