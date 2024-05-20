package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.global.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository, MemberRepository memberRepository) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation save(Long memberId, String date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        Reservation reservation = new Reservation(member, date, time, theme, RESERVED);

        validatePastReservation(LocalDate.parse(date), time);
        validateDuplication(date, timeId, themeId);

        return reservationRepository.save(reservation);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new RoomescapeException("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomescapeException("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private void validatePastReservation(LocalDate date, ReservationTime time) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
        if (date.isEqual(LocalDate.now()) && time.isBeforeNow()) {
            throw new RoomescapeException("과거 예약을 추가할 수 없습니다.");
        }
    }

    private void validateDuplication(String rawDate, Long timeId, Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(new Date(rawDate), timeId,
            themeId)) {
            throw new RoomescapeException("해당 시간에 예약이 이미 존재합니다.");
        }
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom,
        LocalDate dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            throw new RoomescapeException("날짜 조회 범위가 올바르지 않습니다.");
        }
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateIsBetween(themeId, memberId,
            new Date(dateFrom.toString()), new Date(dateTo.toString()));
    }

    public List<Reservation> findMyReservations(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }
}
