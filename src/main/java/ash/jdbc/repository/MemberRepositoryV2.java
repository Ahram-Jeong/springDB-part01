package ash.jdbc.repository;

import ash.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {
    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member (member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 데이터 변경 실행
            return member;
        } catch (SQLException e) {
            log.error("DB error", e);
            throw e;
        } finally {
            close(con, pstmt, null); // 항상 수행되도록 finally에 작성
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

       try {
           con = getConnection();
           pstmt = con.prepareStatement(sql);
           pstmt.setString(1, memberId);
           rs = pstmt.executeQuery();// 데이터 조회 실행

           if (rs.next()) {
               Member member = new Member();
               member.setMemberId(rs.getString("member_id"));
               member.setMoney(rs.getInt("money"));
               return member;
           } else { // 데이터가 없을 경우
               throw new NoSuchElementException("member not found memberId = " + memberId);
           }
       } catch (SQLException e) {
           log.error("DB error", e);
           throw e;
       } finally {
           close(con, pstmt, rs);
       }
    }

    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();// 데이터 조회 실행

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { // 데이터가 없을 경우
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            log.error("DB error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
            // Connection은 여기서 닫지 않음 (세션 유지 해야함)
        }
    }

    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();// 데이터 변경 실행
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("DB error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
            // Connection은 여기서 닫지 않음 (세션 유지 해야함)
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();// 데이터 변경 실행
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("DB error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();// 데이터 변경 실행
        } catch (SQLException e) {
            log.error("DB error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        // 리소스 정리 : 사용 자원들을 역순으로 close
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection = {} , class = {}", con, con.getClass());
        return con;
    }
}
