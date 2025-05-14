package roomescape.member.repository;

public class MemberJdbcRepository {

//    private final JdbcTemplate jdbcTemplate;
//
//    public MemberJdbcRepository(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    public Member save(Member member) {
//        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
//                .withTableName("member")
//                .usingGeneratedKeyColumns("id");
//
//        SqlParameterSource parameters = new MapSqlParameterSource()
//                .addValue("email", member.getEmail())
//                .addValue("role", member.getRole())
//                .addValue("password", member.getPassword())
//                .addValue("name", member.getName());
//        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
//
//        return Member.load(id, member.getName(), member.getRole(), member.getEmail(), member.getPassword());
//    }
//
//    public Optional<Member> findByEmailAndPassword(String email, String password) {
//        String sql = """
//                SELECT id, name, role, email, password
//                FROM member
//                WHERE email = ? AND password = ?
//                """;
//        return jdbcTemplate.query(sql,
//                        (rs, rowNum) -> Member.load(
//                                rs.getLong("id"),
//                                rs.getString("name"),
//                                Role.valueOf(rs.getString("role")),
//                                rs.getString("email"),
//                                rs.getString("password")
//                        ),
//                        email, password)
//                .stream()
//                .findFirst();
//    }
//
//    public Optional<Member> findById(Long memberId) {
//        String sql = """
//                SELECT id, name, role, email, password
//                FROM member
//                WHERE id = ?
//                """;
//        return jdbcTemplate.query(sql,
//                        (rs, rowNum) -> Member.load(
//                                rs.getLong("id"),
//                                rs.getString("name"),
//                                Role.valueOf(rs.getString("role")),
//                                rs.getString("email"),
//                                rs.getString("password")
//                        ), memberId)
//                .stream()
//                .findFirst();
//    }
//
//    public boolean existsByEmail(String email) {
//        String sql = """
//                SELECT EXISTS(
//                    SELECT 1
//                    FROM member
//                    WHERE email = ?
//                )
//                """;
//        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
//    }
//
//    public List<Member> findAll() {
//        String sql = """
//                SELECT id, name, role, email, password
//                FROM member
//                """;
//        return jdbcTemplate.query(sql,
//                (rs, rowNum) -> Member.load(
//                        rs.getLong("id"),
//                        rs.getString("name"),
//                        Role.valueOf(rs.getString("role")),
//                        rs.getString("email"),
//                        rs.getString("password")
//                ));
//    }
}
