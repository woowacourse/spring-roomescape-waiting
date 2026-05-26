package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.dao.MemberDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.TimeDao;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.dto.request.AdminReservationRequestDto;
import roomescape.dto.request.ReservationPatchDto;
import roomescape.dto.response.PageResponse;

@Service
@Transactional(readOnly = true)
public class AdminReservationService {
    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;

    public AdminReservationService(
            ReservationDao reservationDao,
            MemberDao memberDao,
            TimeDao timeDao,
            ThemeDao themeDao
    ) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
    }

    public PageResponse<Reservation> findAll(int page, int size) {
        int offset = page * size;
        List<Reservation> content = reservationDao.findAll(size, offset);
        long totalElements = reservationDao.count();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, totalElements, totalPages, page, size);
    }

    public List<Reservation> findAllByStoreId(Long storeId) {
        return reservationDao.findAllByStoreId(storeId);
    }

    public Reservation findById(Long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }

    @Transactional
    public Reservation createByAdmin(AdminReservationRequestDto request) {
        Member member = memberDao.findById(request.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 멤버입니다."));
        Reservation reservation = buildReservation(member, request);
        return reservationDao.insert(reservation);
    }

    @Transactional
    public Reservation update(Long id, ReservationPatchDto request) {
        Reservation reservation = findById(id);
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        reservation.update(request.date(), time);
        return reservationDao.update(reservation);
    }

    @Transactional
    public void cancelByAdmin(Long id) {
        Reservation reservation = findById(id);
        reservation.cancelByAdmin(LocalDateTime.now());
        reservationDao.update(reservation);
    }

    @Transactional
    public void delete(Long id) {
        if (!reservationDao.existsById(id)) {
            throw new EntityNotFoundException("존재하지 않는 예약입니다.");
        }
        reservationDao.delete(id);
    }

    private Reservation buildReservation(Member member, AdminReservationRequestDto request) {
        Time time = timeDao.findById(request.timeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 시간입니다."));
        Theme theme = themeDao.findById(request.themeId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 테마입니다."));
        if (reservationDao.selectForUpdateByThemeIdAndTimeIdAndDate(request.themeId(), request.timeId(), request.date())) {
            throw new DuplicateEntityException("이미 존재하는 예약이 있습니다.");
        }
        return Reservation.createByAdmin(member, request.date(), time, theme, request.storeId());
    }
}
