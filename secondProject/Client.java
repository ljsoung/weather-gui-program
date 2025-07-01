package basicProjectII.secondProject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client {
    Socket mySocket = null;
    DataOutputStream dataOutStream = null;

    public static void main(String[] args) {
        new Client().startClient();
    }

    public void startClient() {
        try {
            // 서버와 소켓 연결
            mySocket = new Socket("localhost", 1702);
            System.out.println("Client> 서버로 연결되었습니다.");

            // 서버로 데이터 전송을 위한 출력 스트림 초기화
            dataOutStream = new DataOutputStream(mySocket.getOutputStream());

            // GUI 생성
            ClientGui clientGui = new ClientGui(this);

            // 서버로부터 메시지를 수신하는 스레드 시작
            new MessageListener(mySocket, clientGui).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            // 서버로 메시지 전송
            dataOutStream.writeUTF(message);
            dataOutStream.flush();
            System.out.println("Client> 서버로 전송: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 서버 메시지를 수신하는 스레드
class MessageListener extends Thread {
    Socket socket;
    InputStream inStream;
    DataInputStream dataInStream;

    private ClientGui clientGui;

    // 생성자에서 소켓과 GUI 객체를 전달받음
    MessageListener(Socket _s, ClientGui clientGui) {
        this.socket = _s;
        this.clientGui = clientGui;
    }

    public void run() {
        try {
            // 서버로부터 데이터를 읽기 위한 입력 스트림 초기화
            inStream = socket.getInputStream();
            dataInStream = new DataInputStream(inStream);

            while (true) {
                // 서버로부터 메시지 수신
                String msg = dataInStream.readUTF();
                System.out.println("Client> 서버로부터 수신: \n" + msg);

                // 메시지 유형과 내용 분리
                String[] parts = msg.split("\\|", 2); // "|"를 기준으로 메시지 분리
                String messageType = parts[0]; // 메시지 유형 ("검색기록" 또는 "날씨데이터")
                String messageContent = parts.length > 1 ? parts[1] : ""; // 메시지 데이터

                // 검색 기록 처리
                if (messageType.equals("검색기록")) {
                    // JSON 데이터를 파싱하여 검색 기록 리스트로 변환
                    List<List<WeatherResponse.Item>> searchHistory = parseSearchHistoryData(messageContent);

                    // GUI에서 검색 기록 업데이트
                    SwingUtilities.invokeLater(() -> clientGui.searchHistoryPanel.updateSearchHistory(searchHistory));
                }
                // 날씨 데이터 처리
                else if (messageType.equals("날씨데이터")) {
                    // JSON 데이터 파싱
                    List<WeatherResponse.Item> parsedItems = parseWeatherData(messageContent);

                    // 실황 데이터와 예보 데이터를 분리
                    List<WeatherResponse.Item> realTimeData = new ArrayList<>();
                    List<WeatherResponse.Item> forecastData = new ArrayList<>();

                    for (WeatherResponse.Item item : parsedItems) {
                        if (item.getFcstDate() == null) {
                            realTimeData.add(item); // 실황 데이터
                        } else {
                            forecastData.add(item); // 예보 데이터
                        }
                    }

                    // GUI에서 실황 및 예보 데이터 업데이트
                    SwingUtilities.invokeLater(() -> {
                        clientGui.provideWeatherPanel.updateWeatherData(realTimeData);
                        clientGui.provideWeatherPanel.updateShortForecastData(forecastData);
                    });
                }
            }

        } catch (IOException e) {
            System.out.println("Client> 서버 연결이 종료되었습니다.");
        }
    }

    // 서버에서 받은 데이터를 WeatherResponse.Item 리스트로 변환
    private List<WeatherResponse.Item> parseWeatherData(String msg) {
        List<WeatherResponse.Item> weatherItems = new ArrayList<>();
        String[] lines = msg.split("\\n"); // 데이터를 라인 단위로 분리

        for (String line : lines) {
            if (line.startsWith("Item{")) { // 데이터 형식 확인
                WeatherResponse.Item item = new WeatherResponse.Item();

                if (line.contains("obsrValue")) { // 실황 데이터 처리
                    item.setCategory(getValue(line, "category"));
                    item.setObsrValue(Float.parseFloat(getValue(line, "obsrValue")));
                } else { // 예보 데이터 처리
                    item.setCategory(getValue(line, "category"));
                    item.setFcstDate(getValue(line, "fcstDate"));
                    item.setFcstTime(getValue(line, "fcstTime"));
                    item.setFcstValue(Float.parseFloat(getValue(line, "fcstValue")));
                }
                weatherItems.add(item);
            }
        }
        return weatherItems;
    }

    // 특정 키의 값을 라인에서 추출
    private String getValue(String line, String key) {
        int start = line.indexOf(key + "='") + key.length() + 2;
        int end = line.indexOf("'", start);
        return line.substring(start, end);
    }

    // JSON 형식의 검색 기록 데이터를 WeatherResponse.Item 리스트로 변환
    private List<List<WeatherResponse.Item>> parseSearchHistoryData(String json) {
        List<List<WeatherResponse.Item>> searchHistory = new ArrayList<>();
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<List<Map<String, Object>>>>() {}.getType();
            List<List<Map<String, Object>>> rawData = gson.fromJson(json, type);

            // JSON 데이터를 WeatherResponse.Item 리스트로 변환
            for (List<Map<String, Object>> recordGroup : rawData) {
                List<WeatherResponse.Item> recordList = new ArrayList<>();
                for (Map<String, Object> record : recordGroup) {
                    WeatherResponse.Item item = new WeatherResponse.Item();
                    item.setObsrValue((float) ((Number) record.get("obsrValue")).doubleValue()); // 관측 값
                    item.setCategory((String) record.get("category")); // 카테고리
                    item.setCreatedAt((String) record.get("createdAt")); // 검색 시간
                    item.setLevel1((String) record.get("level1")); // 상위 지역
                    item.setLevel2((String) record.get("level2")); // 하위 지역
                    recordList.add(item);
                }
                searchHistory.add(recordList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchHistory;
    }
}
