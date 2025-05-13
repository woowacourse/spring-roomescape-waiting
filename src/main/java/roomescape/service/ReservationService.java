package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationThemeRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationService {

    public static final int DELETE_FAILED_COUNT = 0;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationThemeRepository reservationThemeRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ReservationThemeRepository reservationThemeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationThemeRepository = reservationThemeRepository;
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    public ReservationResponse addReservation(final ReservationRequest request) {
        long timeId = request.timeId();
        final long themeId = request.themeId();

        ReservationTime time = reservationTimeRepository.findById(timeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 시간 입니다."));
        ReservationTheme theme = reservationThemeRepository.findById(themeId).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 테마 입니다."));
        Reservation reservation = new Reservation(request.name(), request.date(), time, theme);
        validateUniqueReservation(reservation);
        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    public void removeReservation(final long id) {
        int deleteCounts = reservationRepository.deleteById(id);
        if (deleteCounts == DELETE_FAILED_COUNT) {
            throw new NoSuchElementException(String.format("[ERROR] 예약번호 %d번은 존재하지 않습니다.", id));
        }
    }

    private void validateUniqueReservation(final Reservation reservation) {
        if (existsSameReservation(reservation)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약시간입니다.");
        }
    }

    private boolean existsSameReservation(final Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findByDate(reservation.getDate());
        return reservations.stream()
                .anyMatch(candidate -> candidate.isDuplicateReservation(reservation));
    }
}
