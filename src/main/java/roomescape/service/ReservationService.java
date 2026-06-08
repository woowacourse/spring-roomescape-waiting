package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UserReservationUpdateRequest;
import roomescape.dto.response.ReservationOrderResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.IdNotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationDao;
    private final ReservationTimeRepository reservationTimeDao;
    private final ThemeRepository themeDao;

    public ReservationService(ReservationRepository reservationDao, ReservationTimeRepository reservationTimeDao, ThemeRepository themeDao) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public List<ReservationOrderResponse> findByName(String name) {
        List<Reservation> myReservations = reservationDao.findByName(name);

        return myReservations.stream()
                .map(reservation -> {
                    long order = calculateOrder(reservation);
                    return new ReservationOrderResponse(
                            reservation.getId(),
                            reservation.getName(),
                            reservation.getDate(),
                            ReservationTimeResponse.from(reservation.getTime()),
                            ThemeResponse.from(reservation.getTheme()),
                            order);
                })
                .toList();
    }

    public List<ReservationOrderResponse> findAll() {
        return reservationDao.findAll().stream()
                .map(reservation -> {
                    long order = calculateOrder(reservation);
                    return new ReservationOrderResponse(
                            reservation.getId(),
                            reservation.getName(),
                            reservation.getDate(),
                            ReservationTimeResponse.from(reservation.getTime()),
                            ThemeResponse.from(reservation.getTheme()),
                            order);
                })
                .toList();
    }

    @Transactional
    public ReservationResponse save(ReservationRequest request, LocalDateTime requestedAt) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);

        Reservation reservation = new Reservation(request.name(), request.date(), time, theme, requestedAt);

        try {
            Reservation saved = reservationDao.save(reservation);
            return ReservationResponse.from(saved);
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException("이미 동일한 예약 또는 대기 신청이 존재합니다.");
        }
    }

    @Transactional
    public ReservationResponse update(Long id, UserReservationUpdateRequest request) {
        ReservationTime time = getValidReservationTime(request.timeId());
        Theme theme = getValidTheme(request.themeId());

        validateReservationDateTime(request.date(), time);

        if (reservationDao.existsBy(request.date(), theme, time)) {
            throw new IllegalArgumentException("요청하신 날짜 및 시간에는 예약이 존재해 변경 불가합니다. 대기를 원하신다면 취소 후 신청해주세요.");
        }

        Reservation newReservation = reservationDao.update(id, request.date(), request.timeId());
        return ReservationResponse.from(newReservation);
    }

    @Transactional
    public void delete(Long id) {
        reservationDao.delete(id);
    }


    private long calculateOrder(Reservation reservation) {
        List<Reservation> sameSlot = reservationDao.findBySlot(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());

        return sameSlot.stream()
                .filter(other -> other.getRequestedAt().isBefore(reservation.getRequestedAt()))
                .count();
    }

    private ReservationTime getValidReservationTime(Long timeId) {
        try {
            return reservationTimeDao.findTimeById(timeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 시간 정보를 찾을 수 없습니다.  선택하신 시간이 정확한지 다시 한번 확인해 주세요.");
        }
    }

    private Theme getValidTheme(Long themeId) {
        try {
            return themeDao.findThemeById(themeId);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("요청하신 테마를 찾을 수 없습니다. 선택하신 테마가 정확한지 다시 한번 확인해 주세요.");
        }
    }

    private void validateReservationDateTime(LocalDate date, ReservationTime time) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time.getStartAt());
        if (targetDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 지난 시간/날짜는 예약할 수 없습니다.");
        }
    }
}
