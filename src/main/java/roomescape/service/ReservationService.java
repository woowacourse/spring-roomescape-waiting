package roomescape.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationRankResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotFoundException;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationRankResponse> find(String name) {
        return reservationRepository.findByName(name)
                .stream()
                .map(ReservationRankResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse save(ReservationRequest request) {
        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                time,
                theme,
                ReservationStatus.PENDING
        );

        reservation.validateNotPast(request.date(), time);
        validateDuplicate(reservation);

        Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse update(Long id, UserReservationUpdateRequest request) {
        Reservation reservation = getReservation(id);
        ReservationTime time = getReservationTime(request.timeId());

        if (reservation.isSameDateTime(request.date(), request.timeId())) {
            return ReservationResponse.from(reservation);
        }

        Reservation newReservation = new Reservation(
                reservation.getName(),
                request.date(),
                time,
                reservation.getTheme(),
                ReservationStatus.PENDING
        );

        newReservation.validateNotPast(request.date(), time);
        validateDuplicate(newReservation);

        reservationRepository.delete(id);
        reservationRepository.update(reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId());

        Reservation saved = reservationRepository.save(newReservation);

        return ReservationResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.delete(id);

        reservationRepository.update(reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId());

    }

    private Reservation getReservation(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("요청하신 예약을 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findTimeById(timeId)
                .orElseThrow(() -> new NotFoundException("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요."));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findThemeById(themeId)
                .orElseThrow(() -> new NotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요."));
    }

    private void validateDuplicate(Reservation reservation) {
        if (reservationRepository.existsByDateAndThemeAndTimeAndName(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getTime().getId(),
                reservation.getName())
        ) {
            throw new AlreadyExistsException("이미 예약되었습니다.");
        }
    }
}

