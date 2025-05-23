package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;

    public ReservationService(JpaReservationRepository reservationRepository,
        JpaReservationTimeRepository reservationTimeRepository,
        JpaThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findReservationsByMemberId(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
            .map(MyReservationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservationsByFilters(Long themeId, Long memberId,
        LocalDate dateFrom, LocalDate dateTo) {
        return reservationRepository.findByFilters(themeId, memberId, dateFrom, dateTo).stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public ReservationResponse addReservationAfterNow(Member member, ReservationRequest request) {
        LocalDate date = request.date();
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        validateDateTimeAfterNow(date, time);

        return addReservation(member, request);
    }

    public ReservationResponse addReservation(Member member, ReservationRequest request) {
        validateDuplicateReservation(request);

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        return ReservationResponse.from(
            reservationRepository.save(new Reservation(member, request.date(), time, theme)));
    }

    private void validateDuplicateReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
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
