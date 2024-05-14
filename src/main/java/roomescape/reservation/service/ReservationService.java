package roomescape.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSearch;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.ReservationTime;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final MemberDao memberDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;


    public ReservationService(ReservationDao reservationDao, MemberDao memberDao, TimeDao timeDao, ThemeDao themeDao) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
    }

    public List<ReservationResponse> findReservations() {
        return reservationDao.findReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservations(ReservationSearchRequest searchRequest) {
        ReservationSearch reservationSearch = searchRequest.createReservationSearch();
        return reservationDao.findReservations(reservationSearch)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Member member = findMemberByMemberId(request.memberId());
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    public ReservationResponse createReservation(ReservationCreateRequest request, Long memberId) {
        Member member = findMemberByMemberId(memberId);
        ReservationTime time = findTimeByTimeId(request.timeId());
        Theme theme = findThemeByThemeId(request.themeId());
        Reservation reservation = request.createReservation(member, time, theme);

        return createReservation(reservation);
    }

    private ReservationResponse createReservation(Reservation reservation) {
        validateIsAvailable(reservation);
        Reservation createdReservation = reservationDao.createReservation(reservation);
        return ReservationResponse.from(createdReservation);
    }

    private Member findMemberByMemberId(Long memberId) {
        return memberDao.findMemberById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버가 존재하지 않습니다."));
    }

    private ReservationTime findTimeByTimeId(Long timeId) {
        return timeDao.findTimeById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme findThemeByThemeId(Long themeId) {
        return themeDao.findThemeById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 테마가 존재하지 않습니다."));
    }

    private void validateIsAvailable(Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약은 현재 시간 이후여야 합니다.");
        }
    }

    public void deleteReservation(Long id) {
        reservationDao.deleteReservation(id);
    }
}
