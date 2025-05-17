package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.repository.JpaReservationRepository;
import roomescape.repository.JpaReservationTimeRepository;
import roomescape.repository.JpaThemeRepository;

@Service
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public ReservationService(
            JpaReservationRepository reservationRepository,
            JpaMemberRepository memberRepository,
            JpaReservationTimeRepository reservationTimeRepository,
            JpaThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findReservationsByMemberId(Member member) {
        return reservationRepository.findByMemberId(member.getId());
    }

    public List<Reservation> findReservationsByFilters(
            long themeId,
            long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationRepository.findAll().stream()
            .filter(r -> r.getTheme().getId().equals(themeId))
            .filter(r -> r.getMember().getId().equals(memberId))
            .filter(r -> r.getDate().isAfter(dateFrom))
            .filter(r -> r.getDate().isBefore(dateTo))
            .toList();
    }

    public Reservation addReservationAfterNow(Member member, ReservationRequest request) {
        LocalDate date = request.date();
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        validateDateTimeAfterNow(date, time);

        return addReservation(member, request);
    }

    public Reservation addReservation(ReservationRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new NotFoundException("member"));

        return addReservation(member, request);
    }

    private Reservation addReservation(Member member, ReservationRequest request) {
        validateDuplicateReservation(request);

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        return reservationRepository.save(
            new Reservation(member, request.date(), time, theme));
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(),
                request.timeId(),
                request.themeId()
        )) {
            throw new DuplicatedException("reservation");
        }
    }

    private void validateDateTimeAfterNow(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now();

        if (date.isBefore(now.toLocalDate()) ||
            (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    public void removeReservation(long id) {
        reservationRepository.deleteById(id);
    }

}
