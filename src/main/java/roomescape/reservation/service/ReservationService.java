package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findReservations(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                                      final LocalDate dateTo) {
        return getReservations(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(reservation -> {
                    ReservationTime time = reservation.getTime();
                    Theme theme = reservation.getTheme();
                    Member member = reservation.getMember();
                    return ReservationResponse.of(reservation, time, theme, member);
                })
                .toList();
    }

    private List<Reservation> getReservations(final Long themeId, final Long memberId,
                                              final LocalDate dateFrom, final LocalDate dateTo) {
        return reservationRepository.findFilteredReservations(themeId, memberId, dateFrom, dateTo);
    }

    public void delete(Long id) {
        if (!reservationRepository.deleteById(id)) {
            throw new ReservationNotFoundException("요청한 id와 일치하는 예약 정보가 없습니다.");
        }
    }

    public ReservationResponse create(final LocalDate date, final Long timeId, final Long themeId, final Long memberId,
                                      final LocalDateTime now) {
        checkIfReservationExists(date, timeId, themeId);
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);
        Member member = findUserByMemberId(memberId);

        Reservation newReservation = reservationRepository.save(
                Reservation.createUpcomingReservationWithUnassignedId(member, date, time, theme, now));
        return ReservationResponse.of(newReservation, time, theme, member);
    }

    private Member findUserByMemberId(final Long memberId) {
        return memberRepository.findUserById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("멤버를 찾을 수 없습니다."));
    }

    private Theme findTheme(final Long request) {
        return themeRepository.findById(request)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 테마 정보가 없습니다."));
    }

    private ReservationTime findReservationTime(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(() -> new ReservationNotFoundException("요청한 id와 일치하는 예약 시간 정보가 없습니다."));
    }

    private void checkIfReservationExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, themeId, timeId)) {
            throw new ReservationAlreadyExistsException("해당 시간에 이미 예약이 존재합니다.");
        }
    }
}
