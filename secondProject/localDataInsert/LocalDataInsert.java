package basicProjectII.secondProject.localDataInsert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.StringTokenizer;

public class LocalDataInsert extends DatabaseHelper{

    String sql; // INSERT 구문을 사용하기 위한 문자열 변수
    BufferedReader br;
    File localFile = new File("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/locals.txt"); // users.txt 파일의 경로를 나타내는 File 객체 생성

    // 한 줄씩 읽은 데이터를 저장할 변수
    String readData;
    StringTokenizer st;

    @Override
    public void insertData() {
        if (!localFile.exists()) {
            System.out.println("파일이 존재하지 않습니다: " + localFile.getAbsolutePath());
            return;
        }

        if (!localFile.canRead()) {
            System.out.println("파일 읽기 권한이 없습니다: " + localFile.getAbsolutePath());
            return;
        }
        try {
            //DB 연결
            con = DriverManager.getConnection(DB_URL, DB_ID, DB_PASSWORD);
            br = new BufferedReader(new FileReader(localFile));

            //파일을 한 줄씩 읽어 들이기
            while ((readData = br.readLine()) != null) {

                //을 기준으로 데이터를 분리
                st = new StringTokenizer(readData, " ");

                // 첫번째 토큰을 level1으로 저장
                String level1 = st.nextToken();
                // level1 20자 넘을 시 오류 메시지 출력 및 삽입 중지
                if (level1.length() > 20) {
                    System.out.println("level1 길이 20자가 넘는 데이터가 있습니다.");
                    continue;
                }

                // 두번째 토큰을 level2로 저장
                String level2 = st.nextToken();
                // level2 20자 넘을 시 오류 메시지 출력 및 삽입 중지
                if (level2.length() > 20) {
                    System.out.println("level2 길이 20자가 넘는 데이터가 있습니다.");
                    continue;
                }

                // 세번째 토큰을 nx로 저장
                int nx = Integer.parseInt(st.nextToken()); // 문자열을 정수형으로 변환하여 저장

                // 네번째 토큰을 ny로 저장
                int ny = Integer.parseInt(st.nextToken()); // 문자열을 정수형으로 변환하여 저장

                // SQL 쿼리 준비 및 실행
                sql = "INSERT INTO locals (level1, level2, nx, ny) VALUES (?, ?, ?, ?)";
                stmt = con.prepareStatement(sql);
                stmt.setString(1, level1);
                stmt.setString(2, level2);
                stmt.setInt(3, nx);
                stmt.setInt(4, ny);

                // 삽입 성공 여부 확인용
                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new local was inserted successfully!");
                }

                stmt.close();
            }
        }catch (IOException e) {
            System.out.println("파일 읽기 오류: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("삽입 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
                if (con != null) con.close();
            } catch (Exception e) {
                System.out.println("리소스를 닫는 중 오류 발생: " + e.getMessage());
            }
        }
    }
}
