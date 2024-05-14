package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;

    public ReservationService(
            final ReservationDao reservationDao,
            final ReservationTimeDao reservationTimeDao,
            final ThemeDao themeDao
    ) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public ReservationResponse save(final ReservationSaveRequest saveRequest, final Member member) {
        ReservationTime reservationTime = findReservationTimeById(saveRequest);
        Theme theme = findThemeById(saveRequest);

        if (hasDuplicateReservation(saveRequest.date(), saveRequest.timeId(), saveRequest.themeId())) {
            throw new IllegalArgumentException("[ERROR] 중복된 예약이 존재합니다.");
        }
        Reservation reservation = saveRequest.toEntity(member, reservationTime, theme);
        return ReservationResponse.from(reservationDao.save(reservation));
    }

    private ReservationTime findReservationTimeById(final ReservationSaveRequest reservationSaveRequest) {
        return reservationTimeDao.findById(reservationSaveRequest.timeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    private Theme findThemeById(final ReservationSaveRequest reservationSaveRequest) {
        return themeDao.findById(reservationSaveRequest.themeId())
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 테마 번호를 입력하였습니다."));
    }

    public List<ReservationResponse> getAll() {
        return reservationDao.getAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<SelectableTimeResponse> findSelectableTimes(final LocalDate date, final long themeId) {
        List<Long> usedTimeIds = reservationDao.findTimeIdsByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeDao.getAll();

        return reservationTimes.stream()
                .map(time -> new SelectableTimeResponse(
                        time.getId(),
                        time.getStartAt(),
                        isAlreadyBooked(time, usedTimeIds)
                ))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<Long> usedTimeIds) {
        return usedTimeIds.contains(reservationTime.getId());
    }

    private boolean hasDuplicateReservation(final LocalDate date, final long timeId, final long themeId) {
        return !reservationDao.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isEmpty();
    }

    public ReservationDeleteResponse delete(final long id) {
        if (reservationDao.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 예약이 존재하지 않습니다.");
        }
        return new ReservationDeleteResponse(reservationDao.delete(id));
    }
}
