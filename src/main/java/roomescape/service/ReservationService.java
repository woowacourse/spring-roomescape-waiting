package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.response.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public List<ReservationResponse> getReservationsByConditions(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findAllByConditions(memberId, themeId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addReservation(
            LocalDate date,
            Long timeId,
            Long themeId,
            Long memberId
    ) {
        Member member = memberRepository.getById(memberId);
        ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        Theme theme = themeRepository.getById(themeId);

        Reservation reservation = new Reservation(date, member, reservationTime, theme);

        validateDuplicatedReservation(reservation);
        validateDateTimeNotPassed(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.from(savedReservation);
    }

    private void validateDateTimeNotPassed(Reservation reservation) {
        if (reservation.isBefore(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("지나간 날짜/시간에 대한 예약은 불가능합니다.");
        }
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        if (reservationRepository.existsByReservation(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getThemeId()
        )) {
            throw new IllegalArgumentException("해당 날짜/시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NoSuchElementException("해당 id의 예약이 존재하지 않습니다.");
        }

        reservationRepository.deleteById(id);
    }
}
