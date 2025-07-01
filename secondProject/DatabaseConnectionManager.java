package basicProjectII.secondProject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnectionManager {
    Connection con = null;
    PreparedStatement stmt = null;

    // DB 정보 기입
    static final String DB_URL = "jdbc:mysql://localhost:3306/weather?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";
    static final String DB_ID = "DB_ID";
    static final String DB_PASSWORD = "DB_PASSWORD";

    public Connection getConnection() {
        // JDBC 드라이브 연결
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("드라이브 로딩 오류 .....");
        }

        try {
            con = DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return con;
    }

    // 데이터베이스에서 level1 데이터를 가져오는 메서드
    public static List<String> getLevel1Data() {
        List<String> level1Data = new ArrayList<>();
        String query = "SELECT DISTINCT level1 FROM locals";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // 결과 데이터를 리스트에 추가
            while (rs.next()) {
                level1Data.add(rs.getString("level1"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return level1Data;
    }

    // level1 선택에 따른 level2값 가져오기
    public static List<String> getLevel2Data(String level1) {
        List<String> level2Data = new ArrayList<>();
        String query = "SELECT level2 FROM locals WHERE level1 = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, level1); // level1 파라미터 바인딩
            ResultSet rs = stmt.executeQuery();

            // 결과 데이터를 리스트에 추가
            while (rs.next()) {
                level2Data.add(rs.getString("level2"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return level2Data;
    }

    //x좌표 y좌표 조회 메서드
    public static String[] getCoordinates(String level1, String level2) {
        String query = "SELECT nx, ny FROM locals WHERE level1 = ? AND level2 = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, level1); // level1 파라미터 설정
            stmt.setString(2, level2); // level2 파라미터 설정
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nx = rs.getString("nx");
                String ny = rs.getString("ny");
                return new String[]{nx, ny};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 값이 없을 경우
    }

    // 초단기실황 데이터 삽입
    public void insertUltraSrtNcstData(WeatherResponse.Item item) {
        String insertQuery = "INSERT INTO ultra_srt_ncst (category, obsrValue, baseDate, baseTime, nx, ny) VALUES (?, ?, ?, ?, ?, ?)";
        String deleteExcessQuery = """
        DELETE FROM ultra_srt_ncst
        WHERE id NOT IN (
            SELECT id FROM (
                SELECT id FROM ultra_srt_ncst ORDER BY id DESC LIMIT 20
            ) AS subquery
        )
    """;

        Connection conn = null; // Connection 변수를 외부에서 선언
        PreparedStatement insertStmt = null;
        PreparedStatement deleteStmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 데이터 삽입
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, item.getCategory()); // 카테고리 설정
            insertStmt.setFloat(2, item.getObsrValue()); // 관측 값 설정
            insertStmt.setString(3, item.getBaseDate()); // 기준 날짜 설정
            insertStmt.setString(4, item.getBaseTime()); // 기준 시간 설정
            insertStmt.setInt(5, item.getNx()); // X 좌표 설정
            insertStmt.setInt(6, item.getNy()); // Y 좌표 설정
            insertStmt.executeUpdate(); // 삽입 쿼리 실행

            // 초과 데이터 삭제
            deleteStmt = conn.prepareStatement(deleteExcessQuery);
            deleteStmt.executeUpdate(); // 초과 데이터 삭제 쿼리 실행

            conn.commit(); // 트랜잭션 커밋
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // 롤백 처리 (오류 발생 시 데이터 복구)
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            // 자원 해제
            try {
                if (deleteStmt != null) deleteStmt.close(); // DELETE 쿼리 닫기
                if (insertStmt != null) insertStmt.close(); // INSERT 쿼리 닫기
                if (conn != null) conn.close(); // DB 연결 종료
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    // 초단기예보 데이터 삽입
    public void insertUltraSrtFcstData(WeatherResponse.Item item) {
        String query = "INSERT INTO ultra_srt_fcst (category, fcstValue, fcstDate, fcstTime, nx, ny) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); // 데이터베이스 연결
             PreparedStatement stmt = conn.prepareStatement(query)) { // 쿼리 준비

            stmt.setString(1, item.getCategory()); // 카테고리 설정
            stmt.setFloat(2, item.getFcstValue()); // 예보 값 설정
            stmt.setString(3, item.getFcstDate()); // 예보 날짜 설정
            stmt.setString(4, item.getFcstTime()); // 예보 시간 설정
            stmt.setInt(5, item.getNx()); // X 좌표 설정
            stmt.setInt(6, item.getNy()); // Y 좌표 설정
            stmt.executeUpdate(); // 삽입 쿼리 실행

        } catch (SQLException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
        }
    }

    // 단기예보 데이터 삽입
    public void insertVilageFcstData(WeatherResponse.Item item) {
        String query = "INSERT INTO vilage_fcst (category, fcstValue, fcstDate, fcstTime, nx, ny) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); // 데이터베이스 연결
             PreparedStatement stmt = conn.prepareStatement(query)) { // 쿼리 준비

            stmt.setString(1, item.getCategory()); // 카테고리 설정
            stmt.setFloat(2, item.getFcstValue()); // 예보 값 설정
            stmt.setString(3, item.getFcstDate()); // 예보 날짜 설정
            stmt.setString(4, item.getFcstTime()); // 예보 시간 설정
            stmt.setInt(5, item.getNx()); // X 좌표 설정
            stmt.setInt(6, item.getNy()); // Y 좌표 설정
            stmt.executeUpdate(); // 삽입 쿼리 실행

        } catch (SQLException e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스 출력
        }
    }

    public List<List<Map<String, Object>>> getGroupedUltraSrtNcstDataWithLocation() {
        String query = """
    SELECT 
        usn.category, 
        usn.obsrValue, 
        usn.createdAt,
        loc.level1, 
        loc.level2
    FROM ultra_srt_ncst usn
    JOIN locals loc
    ON usn.nx = loc.nx AND usn.ny = loc.ny
    """;
        List<List<Map<String, Object>>> groupedData = new ArrayList<>();

        try (Connection conn = getConnection(); // 데이터베이스 연결
             PreparedStatement stmt = conn.prepareStatement(query); // 쿼리 준비
             ResultSet rs = stmt.executeQuery()) { // 쿼리 실행 및 결과 저장

            List<Map<String, Object>> currentGroup = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>(); // 행 데이터를 저장할 맵 생성
                row.put("category", rs.getString("category")); // 카테고리 데이터 저장
                row.put("obsrValue", rs.getFloat("obsrValue")); // 관측 값 저장
                row.put("createdAt", rs.getString("createdAt")); // 생성 일자 저장
                row.put("level1", rs.getString("level1")); // 지역 level1 데이터 저장
                row.put("level2", rs.getString("level2")); // 지역 level2 데이터 저장

                currentGroup.add(row); // 현재 그룹에 추가

                // 그룹 크기가 4개에 도달하면 groupedData에 추가
                if (currentGroup.size() == 4) {
                    groupedData.add(new ArrayList<>(currentGroup)); // 현재 그룹을 복사하여 추가
                    currentGroup.clear(); // 현재 그룹 초기화
                }
            }

            // 남아 있는 데이터 처리
            if (!currentGroup.isEmpty()) {
                groupedData.add(currentGroup); // 마지막 그룹 추가
            }

        } catch (SQLException e) {
            e.printStackTrace(); // SQL 예외 출력
        }

        return groupedData; // 그룹화된 데이터 반환
    }
}
