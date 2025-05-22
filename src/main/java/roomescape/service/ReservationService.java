package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.entity.*;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaWaitingRepository waitingRepository;

    public ReservationService(
            JpaReservationRepository reservationRepository,
            JpaMemberRepository memberRepository,
            JpaReservationTimeRepository reservationTimeRepository,
            JpaThemeRepository themeRepository,
            JpaWaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<WaitingWithRank> findAllWaiting() {
        return waitingRepository.findAllWaitingWithRank();
    }

    @Transactional(readOnly = true)
    public List<WaitingWithRank> findAllWaitingWithRankByMemberId(Member member) {
        return waitingRepository.findWaitingsWithRankByMemberId(member.getId());
    }

    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByMemberId(Member member) {
        return reservationRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new NotFoundException("reservations"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByFilters(
            long themeId,
            long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationRepository.findReservationsByFilters(themeId, memberId, dateFrom, dateTo)
                .orElseThrow(() -> new NotFoundException("reservation"));
    }

    @Transactional
    public Reservation addReservation(Member member, ReservationRequest request) {
        LocalDate date = request.date();
        long timeId = request.timeId();
        long themeId  = request.themeId();

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("theme"));

        validateDuplicateReservation(date,timeId,themeId);
        validateDateTimeAfterNow(date, time);
        return reservationRepository.save(new Reservation(member, request.date(), time, theme));
    }

    @Transactional
    public Waiting addWaiting(Member member, ReservationRequest request) {
        LocalDate date = request.date();
        long timeId = request.timeId();
        long themeId  = request.themeId();

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("theme"));

        validateDateTimeAfterNow(date, time);
        return waitingRepository.save(new Waiting(member, date, time, theme));
    }

    private void validateDateTimeAfterNow(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (date.isBefore(now.toLocalDate()) ||
                (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    @Transactional(readOnly = true)
    private void validateDuplicateReservation(
            final LocalDate date,
            final long timeId,
            final long themeId
    ) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new DuplicatedException("reservation");
        }
    }

    @Transactional
    public void removeReservation(long id) {
        if (reservationRepository.existsById(id)) {
            throw new NotFoundException("reservation");
        }
        reservationRepository.deleteById(id);
    }
}
