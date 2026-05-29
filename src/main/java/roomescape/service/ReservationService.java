package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationOrder;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.NotFoundException;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public List<ReservationOrderResponse> find(String name) {
        List<ReservationOrder> response = reservationDao.findByName(name);
        return response.stream()
                .map(ReservationOrderResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll()
                .stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse save(ReservationRequest request) {
        ReservationTime time = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        validateReservationDateTime(request.date(), time);
        ReservationStatus status = checkReservationStatus(request.date(), theme, time);

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                time,
                theme,
                status
        );

        validateDuplicate(reservation);

        Reservation saved = reservationDao.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse update(Long id, UserReservationUpdateRequest request) {
        Reservation reservation = getReservation(id);
        ReservationTime time = getReservationTime(request.timeId());

        reservationDao.delete(id);

        validateReservationDateTime(request.date(), time);
        ReservationStatus status = checkReservationStatus(request.date(), reservation.getTheme(), time);

        Reservation newReservation = new Reservation(
                id,
                reservation.getName(),
                request.date(),
                time,
                reservation.getTheme(),
                status
        );

        validateDuplicate(newReservation);
        reservationDao.save(newReservation);

        return ReservationResponse.from(newReservation);
    }

    @Transactional
    public void delete(Long id) {
        reservationDao.delete(id);
    }

    private Reservation getReservation(long id) {
        Reservation reservation = reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("요청하신 예약을 찾을 수 없습니다."));
        return reservation;
    }

    private ReservationTime getReservationTime(Long timeId) {
        ReservationTime time = reservationTimeDao.findTimeById(timeId)
                .orElseThrow(() -> new NotFoundException("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요."));
        return time;
    }

    private Theme getTheme(Long themeId) {
        Theme theme = themeDao.findThemeById(themeId)
                .orElseThrow(() -> new NotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요."));
        return theme;
    }

    private void validateReservationDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 지난 시간/날짜는 예약할 수 없습니다.");
        }
    }

    private void validateDuplicate(Reservation reservation) {
        if (reservationDao.existsByDateAndThemeAndTimeAndName(
                reservation.getDate(),
                reservation.getTheme().getId(),
                reservation.getTime().getId(),
                reservation.getName())
        ) {
            throw new AlreadyExistsException("이미 예약되었습니다.");
        }
    }

    private ReservationStatus checkReservationStatus(LocalDate date, Theme theme, ReservationTime time) {
        if (reservationDao.existsByDateAndThemeAndTime(date, theme, time)) {
            return ReservationStatus.WAITING;
        }

        return ReservationStatus.CONFIRMED;
    }
}

