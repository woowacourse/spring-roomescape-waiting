package roomescape.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.repository.ReservationRepository;

@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationQueryService reservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;

    public ReservationCommandService(
            ReservationRepository reservationRepository,
            ReservationQueryService reservationQueryService,
            ReservationTimeQueryService reservationTimeQueryService,
            ThemeQueryService themeQueryService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationQueryService = reservationQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
    }

    @Transactional
    public Reservation save(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeQueryService.getById(request.timeId());
        Theme theme = themeQueryService.getById(request.themeId());
        Reservation reservation = Reservation.createWith(
                request.name(),
                request.date(),
                reservationTime,
                theme,
                LocalDateTime.now()
        );

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateMine(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationQueryService.getById(id);
        ReservationTime newTime = reservationTimeQueryService.getById(request.timeId());
        Reservation updated = existing.updateWith(
                name,
                request.date(),
                newTime,
                LocalDateTime.now()
        );

        return reservationRepository.update(updated);
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteMine(Long id, String name) {
        Reservation reservation = reservationQueryService.getById(id);
        reservation.cancelBy(name, LocalDateTime.now());

        reservationRepository.deleteById(id);
    }
}
