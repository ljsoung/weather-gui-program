package basicProjectII.secondProject;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;


public class Server {
    ServerSocket ss = null;

    public static void main(String[] args) {
        Server server = new Server();

        try {
            server.ss = new ServerSocket(1702); // 서버 소켓 생성
            System.out.println("Server> Server Socket is created....");

            while (true) {
                Socket socket = server.ss.accept(); // 클라이언트 연결 대기
                new ConnectedClient(socket).start(); // 새로운 스레드 생성
            }
        } catch (SocketException e) {
            System.out.println("Server> 서버 종료");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ConnectedClient extends Thread {
    Socket socket;
    OutputStream outStream;
    DataOutputStream dataOutStream;
    InputStream inStream;
    DataInputStream dataInStream;
    DatabaseConnectionManager dbManager = new DatabaseConnectionManager();

    public ConnectedClient(Socket socket) {
        this.socket = socket;
    }

    // 날씨 데이터를 데이터베이스에 저장하는 메서드
    private void saveWeatherDataToDB(List<WeatherResponse.Item> data, String type) {
        // 데이터를 순회하며 각 항목을 처리
        for (WeatherResponse.Item item : data) {
            switch (type) {
                case "ULTRA_SRT_NCST": // 초단기실황 데이터
                    dbManager.insertUltraSrtNcstData(item); // 초단기실황 데이터를 DB에 삽입
                    break;
                case "ULTRA_SRT_FCST": // 초단기예보 데이터
                    dbManager.insertUltraSrtFcstData(item); // 초단기예보 데이터를 DB에 삽입
                    break;
                case "VILAGE_FCST": // 단기예보 데이터
                    dbManager.insertVilageFcstData(item); // 단기예보 데이터를 DB에 삽입
                    break;
            }
        }
    }


    @Override
    public void run() {
        try {
            System.out.println("Server> 클라이언트 연결됨: " + socket.toString());

            outStream = this.socket.getOutputStream();
            dataOutStream = new DataOutputStream(outStream);
            inStream = this.socket.getInputStream();
            dataInStream = new DataInputStream(inStream);

            WeatherApiCaller apiCaller = new WeatherApiCaller(); // API 호출 객체 생성
            WeatherDataParser dataParser = new WeatherDataParser(); // 데이터 파싱 객체 생성
            Gson gson = new Gson(); // JSON 변환을 위한 Gson 객체

            while (true) {
                // 클라이언트로부터 메시지 수신
                String receivedMessage = dataInStream.readUTF();
                System.out.println("Server> 클라이언트로부터 받은 메시지: " + receivedMessage);

                if(receivedMessage.equals("검색기록")){
                    try {
                        // DB 접근 및 데이터 조회
                        List<List<Map<String, Object>>> searchHistory = dbManager.getGroupedUltraSrtNcstDataWithLocation();

                        // JSON 형식으로 변환하여 클라이언트로 전송
                        String jsonResponse = gson.toJson(searchHistory);

                        System.out.println("JSON Response: " + jsonResponse);
                        // 메시지 앞에 "검색기록" 태그 추가
                        String response = "검색기록|" + jsonResponse;

                        dataOutStream.writeUTF(response);
                        dataOutStream.flush();
                        System.out.println("Server> 검색 기록 전송 완료.");
                    } catch (Exception e) {
                        // 예외 발생 시 에러 메시지 전송
                        dataOutStream.writeUTF("검색기록|검색 기록을 가져오는 중 오류가 발생했습니다.");
                        dataOutStream.flush();
                        e.printStackTrace();
                    }
                } else{
                    try {
                        // 메시지 파싱 (좌표 값 추출)
                        String[] parts = receivedMessage.split(",");
                        int nx = Integer.parseInt(parts[0].trim());
                        int ny = Integer.parseInt(parts[1].trim());

                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime baseTimeNow = now.withMinute(0).withSecond(0);

                        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd")); // 현재 날짜 기준
                        String baseTimeUltraSrtNcst = baseTimeNow.format(DateTimeFormatter.ofPattern("HHmm")); // 초단기실황 기준 시간
                        String baseTimeUltraSrtFcst = calculateUltraSrtFcstBaseTime(); // 초단기예보 기준 시간
                        String baseTimeSrtFcst = calculateBaseTime(); // 단기예보 기준 시간

                        StringBuilder fullResponse = new StringBuilder();

                        // 초단기실황 데이터
                        String ultraSrtNcstResponse = apiCaller.fetchWeatherData("ULTRA_SRT_NCST", baseDate, baseTimeUltraSrtNcst, nx, ny);
                        List<WeatherResponse.Item> ultraSrtNcstData = dataParser.parseAndFilterWeatherData(ultraSrtNcstResponse, "ULTRA_SRT_NCST");
                        appendWeatherData("초단기실황", ultraSrtNcstData, fullResponse);

                        // 초단기예보 데이터
                        String ultraSrtFcstResponse = apiCaller.fetchWeatherData("ULTRA_SRT_FCST", baseDate, baseTimeUltraSrtFcst, nx, ny);
                        List<WeatherResponse.Item> ultraSrtFcstData = dataParser.parseAndFilterWeatherData(ultraSrtFcstResponse, "ULTRA_SRT_FCST");
                        appendWeatherData("초단기예보", ultraSrtFcstData, fullResponse);

                        // 단기예보 데이터
                        String vilageFcstResponse = apiCaller.fetchWeatherData("VILAGE_FCST", baseDate, baseTimeSrtFcst, nx, ny);
                        List<WeatherResponse.Item> vilageFcstData = dataParser.parseAndFilterWeatherData(vilageFcstResponse, "VILAGE_FCST");
                        appendWeatherData("단기예보", vilageFcstData, fullResponse);

                        // DB 저장
                        saveWeatherDataToDB(ultraSrtNcstData, "ULTRA_SRT_NCST");
                        saveWeatherDataToDB(ultraSrtFcstData, "ULTRA_SRT_FCST");
                        saveWeatherDataToDB(vilageFcstData, "VILAGE_FCST");

                        // 메시지 앞에 "날씨데이터" 태그 추가
                        if (fullResponse.length() > 0) {
                            String response = "날씨데이터|" + fullResponse.toString();
                            dataOutStream.writeUTF(response);
                        } else {
                            dataOutStream.writeUTF("날씨데이터|해당 좌표의 날씨 정보를 찾을 수 없습니다.");
                        }
                        dataOutStream.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Server> 클라이언트 연결 종료: " + socket.toString());
        } finally {
            try {
                if (dataInStream != null) dataInStream.close(); // 데이터 입력 스트림 닫기
                if (dataOutStream != null) dataOutStream.close(); // 데이터 출력 스트림 닫기
                if (socket != null) socket.close(); // 소켓 닫기
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 날씨 데이터 추가 메서드
    private void appendWeatherData(String type, List<WeatherResponse.Item> data, StringBuilder fullResponse) {
        // 데이터가 비어 있지 않은 경우
        if (!data.isEmpty()) {
            // 데이터 타입 헤더 추가
            fullResponse.append("=== ").append(type).append(" ===\n");
            // 각 데이터를 문자열로 추가
            for (WeatherResponse.Item item : data) {
                fullResponse.append(item.toString()).append("\n");
            }
        } else {
            // 데이터가 없는 경우 메시지 추가
            fullResponse.append(type).append(" 데이터가 없습니다.\n");
        }
    }

    //단기예보 시간 계산
    public static String calculateBaseTime() {
        // 현재 날짜 및 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        // 단기 예보 기준 시간 목록 (2시간 간격)
        int[] baseTimes = {2, 5, 8, 11, 14, 17, 20, 23};
        int currentHour = now.getHour(); // 현재 시간
        int currentMinute = now.getMinute(); // 현재 분

        int selectedBaseTime = -1; // 선택된 기준 시간 초기값

        // 기준 시간 계산
        for (int baseTime : baseTimes) {
            // 현재 시간이 기준 시간보다 크거나 같으면 해당 시간 선택
            if (currentHour > baseTime || (currentHour == baseTime && currentMinute >= 10)) {
                selectedBaseTime = baseTime;
            }
        }

        // 현재 시간이 가장 이른 기준 시간보다 이전이라면 전날의 마지막 기준 시간 선택
        if (selectedBaseTime == -1) {
            selectedBaseTime = baseTimes[baseTimes.length - 1];
            now = now.minusDays(1); // 전날로 이동
        }

        // 기준 시간을 HHmm 형식으로 변환
        String baseTimeSrtFcst = String.format("%02d00", selectedBaseTime);

        // 디버깅 정보 출력
        System.out.println("Current Time: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Selected Base Time: " + baseTimeSrtFcst);

        return baseTimeSrtFcst;
    }

    //초단기예보 시간 계산
    public static String calculateUltraSrtFcstBaseTime() {
        // 현재 시간 가져오기
        LocalDateTime now = LocalDateTime.now();
        int minute = now.getMinute(); // 현재 분

        // 분이 45분 이전이면 이전 시간의 30분 사용
        if (minute < 45) {
            now = now.minusHours(1); // 한 시간 전으로 설정
        }

        // 기준 시간을 HH30 형식으로 설정
        String baseTime = String.format("%02d30", now.getHour());

        // 디버깅 정보 출력
        System.out.println("Current Time: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.println("Selected Base Time (UltraSrtFcst): " + baseTime);

        return baseTime;
    }

}
