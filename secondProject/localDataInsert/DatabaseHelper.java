package basicProjectII.secondProject.localDataInsert;


import java.sql.*;

public abstract class DatabaseHelper {

    Connection con = null;
    PreparedStatement stmt = null;

    // DB 정보 기입
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/weather?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";
    protected static final String DB_ID = "root";
    protected static final String DB_PASSWORD = "0000";

    //JDBC 드라이브 연결
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("드라이브 로딩 오류 .....");
        }
    }
    //상속받은 클래스에서 구현
    public abstract void insertData ();
}

