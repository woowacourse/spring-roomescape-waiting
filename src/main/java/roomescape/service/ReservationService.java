package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.IdNotFoundException;
import roomescape.exception.NameNotFoundException;

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

    public ReservationResponse find(String name) {
        Reservation response = reservationDao.findByName(name)
                .orElseThrow(() -> new NameNotFoundException("해당 이름의 예약이 존재하지 않습니다"));
        return ReservationResponse.from(response);
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll()
                .stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    public ReservationResponse save(ReservationRequest request) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);
        validateNoDuplicateReservation(request.date(), theme, time);

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                time,
                theme,
                //TODO
                ReservationStatus.CONFIRMED
        );

        Reservation saved = reservationDao.save(reservation);
        return ReservationResponse.from(saved);
    }

    public ReservationResponse update(Long id, UserReservationUpdateRequest request) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);
        validateNoDuplicateReservation(request.date(), theme, time);

        Reservation newReservation = reservationDao.update(id, request.date(), request.timeId());
        return ReservationResponse.from(newReservation);
    }

    public void delete(Long id) {
        reservationDao.delete(id);
    }

    private ReservationTime getValidReservationTime(Long timeId) {
        ReservationTime time = reservationTimeDao.findTimeById(timeId);
        if (time == null) {
            throw new IdNotFoundException("요청하신 시간 정보를 찾을 수 없습니다. 선택하신 시간이 정확한지 다시 한번 확인해 주세요.");
        }
        return time;
    }

    private Theme getValidTheme(Long themeId) {
        Theme theme = themeDao.findThemeById(themeId);
        if (theme == null) {
            throw new IdNotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요.");
        }
        return theme;
    }

    private void validateReservationDateTime(java.time.LocalDate date, ReservationTime time) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 지난 시간/날짜는 예약할 수 없습니다.");
        }
    }

    private void validateNoDuplicateReservation(java.time.LocalDate date, Theme theme, ReservationTime time) {
        if (reservationDao.existsBy(date, theme, time)) {
            throw new IllegalArgumentException("이미 존재하는 예약 건입니다.");
        }
    }
}
