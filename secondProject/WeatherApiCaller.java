package basicProjectII.secondProject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WeatherApiCaller {

    // API 요청에 필요한 기본 URL
    private static final String BASE_URL_ULTRA_SRT_NCST = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"; // 초단기실황조회
    private static final String BASE_URL_ULTRA_SRT_FCST = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"; // 초단기예보조회
    private static final String BASE_URL_VILAGE_FCST = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"; // 단기예보조회
    private static final String SERVICE_KEY = "API_KEY";

    // URL 기본 파라미터
    private static final String PAGE_NO = "1";
    private static final String NUM_OF_ROWS = "1000";
    private static final String DATA_TYPE = "JSON";

    public String fetchWeatherData(String apiType, String baseDate, String baseTime, int nx, int ny) throws IOException {

        // API 유형에 따른 URL 설정
        String baseUrl;
        switch (apiType) {
            case "ULTRA_SRT_NCST": // 초단기실황조회
                baseUrl = BASE_URL_ULTRA_SRT_NCST;
                break;
            case "ULTRA_SRT_FCST": // 초단기예보조회
                baseUrl = BASE_URL_ULTRA_SRT_FCST;
                break;
            case "VILAGE_FCST": // 단기예보조회
                baseUrl = BASE_URL_VILAGE_FCST;
                break;
            default:
                throw new IllegalArgumentException("Invalid API type: " + apiType);
        }

        // URL 생성: 기본 URL에 쿼리 파라미터 추가
        StringBuilder urlBuilder = new StringBuilder(baseUrl);

        // 서비스 키 추가 (URL 인코딩 필수)
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + SERVICE_KEY); // 서비스 키

        // 기타 파라미터 추가 (페이지 번호, 데이터 형식, 기준 날짜/시간, 좌표)
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(PAGE_NO, "UTF-8")); // 페이지 번호
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(NUM_OF_ROWS, "UTF-8")); // 결과 수
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(DATA_TYPE, "UTF-8")); // JSON 형식
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")); // 날짜
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")); // 시간
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nx), "UTF-8")); // X 좌표
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(ny), "UTF-8")); // Y 좌표

        // URL 객체 생성
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            // HTTP 연결 객체 생성 및 설정
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setConnectTimeout(5000); // 연결 타임아웃 (5초)
            conn.setReadTimeout(5000); // 읽기 타임아웃 (5초)

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            System.out.println("WeatherApiCaller> Response code: " + responseCode);

            // 응답 데이터 읽기
            reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode <= 300
                            ? conn.getInputStream()
                            : conn.getErrorStream()
            ));

            // 응답 데이터를 StringBuilder에 저장
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 디버깅 용도로 응답 출력
            System.out.println("WeatherApiCaller> Response data: " + response);

            // JSON 응답 반환
            return response.toString();
        } finally {
            // 리소스 정리
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }
}