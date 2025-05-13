package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlotTimes;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRanking;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.AvailableTimeRequestDto;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.exception.reservation.InvalidReservationTimeException;
import roomescape.exception.reservation.InvalidThemeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservation.ReservationTimeRepository;
import roomescape.repository.reservation.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public long addReservation(AddReservationDto newReservation) {
        ReservationTime reservationTime = reservationTimeRepository.findById(newReservation.timeId())
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간 id입니다."));
        Theme theme = themeRepository.findById(newReservation.themeId())
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마 id입니다."));

        Reservation reservation = newReservation.toReservation(reservationTime, theme);

        validateDuplicateReservation(reservation);
        LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
        validateAddReservationDateTime(reservation, currentDateTime);
        return reservationRepository.save(reservation);
    }

    private void validateDuplicateReservation(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeAndTheme(reservation)) {
            throw new InvalidReservationException("중복된 예약신청입니다");
        }
    }

    private void validateAddReservationDateTime(Reservation newReservation, LocalDateTime currentDateTime) {
        if (newReservation.isBeforeDateTime(currentDateTime)) {
            throw new InvalidReservationException("과거 시간에 예약할 수 없습니다.");
        }
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<Reservation> allReservations() {
        return reservationRepository.findAll();
    }

    public ReservationSlotTimes availableReservationTimes(AvailableTimeRequestDto availableTimeRequestDto) {
        List<ReservationTime> times = reservationTimeRepository.findAll();

        List<Reservation> alreadyReservedReservations = reservationRepository.findAllByDateAndThemeId(
                availableTimeRequestDto.date(), availableTimeRequestDto.themeId());

        return new ReservationSlotTimes(times, alreadyReservedReservations);
    }

    public Reservation getReservationById(long addedReservationId) {
        return reservationRepository.findById(addedReservationId)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약입니다."));
    }

    public List<Theme> getRankingThemes(LocalDate originDate, int themeRankingStartRange, int themeRankingEndRange) {
        LocalDate end = originDate.minusDays(themeRankingStartRange);
        LocalDate start = end.minusDays(themeRankingEndRange);
        List<Reservation> inRangeReservations = reservationRepository.findAllByDateInRange(start, end);

        ThemeRanking themeRanking = new ThemeRanking(inRangeReservations);
        return themeRanking.getAscendingRanking();
    }

    public Optional<Reservation> searchReservation(long reservationId, long themeId, LocalDate dateFrom,
                                                   LocalDate dateTo) {
        Optional<Reservation> reservation = reservationRepository.findById(reservationId);
        if (reservation.isEmpty()) {
            return Optional.empty();
        }
        Reservation currentReservation = reservation.get();

        if (currentReservation.isSameTheme(themeId) && currentReservation.isBetweenDate(dateFrom, dateTo)) {
            return reservation;
        }
        return Optional.empty();
    }
}
