package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository, MemberRepository memberRepository,
        ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findReservationsByFilters(Long themeId, Long memberId,
        LocalDate dateFrom, LocalDate dateTo) {
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
        if (reservationRepository. existsByDateAndTimeIdAndThemeId(
            request.date(), request.timeId(), request.themeId())) {
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

    public void removeReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
