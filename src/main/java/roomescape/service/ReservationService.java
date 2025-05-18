package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.global.ReservationStatus;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            MemberRepository memberRepository,
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation addReservation(
            CreateReservationRequest request,
            LoginMemberRequest loginMemberRequest
    ) {
        return createReservation(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId());
    }

    public Reservation addReservationByAdmin(AdminCreateReservationRequest request) {
        return createReservation(request.memberId(), request.themeId(), request.date(), request.timeId());
    }

    private Reservation createReservation(
            long memberId,
            long themeId,
            LocalDate date,
            long timeId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        Reservation reservation = new Reservation(member, date, reservationTime, theme, ReservationStatus.RESERVED);

        validateDuplicateReservation(reservation);
        validateAddReservationDateTime(reservation);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findAllByFilter(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return reservationRepository.findAllByFilter(memberId, themeId, dateFrom, dateTo);
    }

    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<Reservation> findAllReservationByMember(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }

    private void validateDuplicateReservation(Reservation reservation) {
        boolean exists = reservationRepository.existsByTimeIdAndThemeIdAndDate(
                reservation.getReservationTime().getId(),
                reservation.getTheme().getId(),
                reservation.getDate()
        );
        if (exists) {
            throw new InvalidReservationException("중복된 예약신청입니다");
        }
    }

    private void validateAddReservationDateTime(Reservation reservation) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDateTime currentDateTime = ZonedDateTime.now(zoneId).toLocalDateTime();

        if (reservation.isBefore(currentDateTime)) {
            throw new InvalidReservationException("과거 시간에 예약할 수 없습니다.");
        }
    }
}
