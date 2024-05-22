package roomescape.service.dbservice;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationDbService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationDbService(
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository timeRepository,
            ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Reservation findReservation(LocalDate date, long themeId, long timeId) {
        return reservationRepository.findByDateAndThemeAndTime(date, findThemeById(themeId), findTimeById(timeId)).orElseThrow(() -> new RoomEscapeBusinessException("예약이 없습니다"));
    }

    public List<Reservation> findMemberReservations(Member member) {
        return reservationRepository.findByMember(member);
    }

    public Reservation createReservation(long memberId, LocalDate date, long timeId, long themeId) {
        return new Reservation(findMemberById(memberId), date, findTimeById(timeId), findThemeById(themeId));
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public boolean hasReservation(Reservation reservation) {
        return reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    public void delete(long id) {
        reservationTimeRepository.deleteById(id);
    }

    public List<Reservation> findByConditions(
            Optional<LocalDate> start,
            Optional<LocalDate> end,
            Long themeId,
            Long memberId
    ){
        return reservationRepository.findByConditions(start, end, themeId, memberId);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }


}
