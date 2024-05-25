package roomescape.service.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.exception.customexception.RoomEscapeBusinessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
        return new Reservation(findMemberById(memberId), new ReservationSlot(date, findTimeById(timeId), findThemeById(themeId)));
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

    public void delete(Reservation reservation) {
        reservationRepository.delete(reservation);
    }

    public List<Reservation> findByConditions(
            LocalDate start,
            LocalDate end,
            Long themeId,
            Long memberId
    ) {
        return reservationRepository.findByConditions(
                    Optional.ofNullable(start),
                    Optional.ofNullable(end),
                    themeId,
                    memberId
        );
    }

    public Reservation findById(long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("예약이 존재하지 않습니다."));
    }


    public List<Reservation> findAllReservation() {
        return reservationRepository.findAll();
    }

    public Member findMemberById(Long memberId) {
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
