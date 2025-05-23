package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidInputException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;

@Service
@Transactional
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;

    public ReservationService(JpaReservationRepository reservationRepository,
        JpaReservationTimeRepository reservationTimeRepository,
        JpaThemeRepository themeRepository, JpaMemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll().stream()
            .map(ReservationResponse::from)
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

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        validateDateTimeAfterNow(date, time);

        return ReservationResponse.from(
            reservationRepository.save(new Reservation(member, request.date(), time, theme)));
    }

    public ReservationResponse addReservation(ReservationRequest request) {
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new NotFoundException("member"));

        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException("time"));

        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException("theme"));

        return ReservationResponse.from(
            reservationRepository.save(new Reservation(member, request.date(), time, theme)));
    }

    private void validateDateTimeAfterNow(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        if (date.isBefore(now.toLocalDate()) ||
            (date.isEqual(now.toLocalDate()) && time.isBefore(now.toLocalTime()))) {
            throw new InvalidInputException("과거 예약은 불가능");
        }
    }

    public void removeReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
