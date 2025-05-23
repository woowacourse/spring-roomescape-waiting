package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.repository.JpaMemberRepository;
import roomescape.reservation.controller.dto.request.ReservationRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservationTime.entity.ReservationTime;
import roomescape.reservationTime.repository.JpaReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.JpaThemeRepository;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.waiting.repository.JpaWaitingRepository;

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
        return reservationRepository.save(new Reservation(member, date, time, theme));
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
