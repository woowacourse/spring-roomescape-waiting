package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.global.exception.DuplicateSaveException;
import roomescape.global.exception.IllegalReservationDateException;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.dto.MemberReservationAddRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Service
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findAllReservation() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(Long memberId, Long themeId, LocalDate dateFrom,
                                                                      LocalDate dateTo) {
        return reservationRepository.findAllByMemberAndThemeAndPeriod(memberId, themeId, dateFrom, dateTo).stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public ReservationResponse saveMemberReservation(Long memberId, MemberReservationAddRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchRecordException("ID: " + memberId + " 해당하는 회원을 찾을 수 없습니다"));

        return saveMemberReservation(member, request);
    }

    public ReservationResponse saveMemberReservation(Member member, MemberReservationAddRequest request) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId())) {
            throw new DuplicateSaveException("중복되는 예약이 존재합니다");
        }

        ReservationTime reservationTime = getReservationTime(request.timeId());
        Theme theme = getTheme(request.themeId());

        Reservation reservation = request.toReservation(member, reservationTime, theme);
        if (reservation.isDateBefore(LocalDate.now())) {
            throw new IllegalReservationDateException(reservation.getDate() + ": 예약 날짜는 현재 보다 이전일 수 없습니다");
        }
        Reservation saved = reservationRepository.save(reservation);
        return new ReservationResponse(saved);
    }

    private ReservationTime getReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 예약시간이 존재하지 않습니다 ID: " + timeId));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NoSuchRecordException("해당하는 테마가 존재하지 않습니다 ID: " + themeId));
    }

    public void removeReservation(long id) {
        reservationRepository.deleteById(id);
    }
}
