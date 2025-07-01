package basicProjectII.secondProject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WeatherDataParser {

    private static final Set<String> ULTRA_SRT_NCST_CATEGORIES = Set.of("T1H", "PTY", "WSD", "REH"); // 초단기실황
    private static final Set<String> ULTRA_SRT_FCST_CATEGORIES = Set.of("T1H", "PTY"); // 초단기예보
    private static final Set<String> VILAGE_FCST_CATEGORIES = Set.of("TMN", "TMX"); // 단기예보

    public List<WeatherResponse.Item> parseAndFilterWeatherData(String jsonResponse, String apiType) {
        List<WeatherResponse.Item> filteredItems = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject response = root.optJSONObject("response");
            if (response == null || !"00".equals(response.getJSONObject("header").optString("resultCode"))) {
                System.err.println("WeatherDataParser> Invalid or failed API response.");
                return filteredItems;
            }

            JSONObject body = response.optJSONObject("body");
            if (body == null) {
                System.err.println("WeatherDataParser> No body in response.");
                return filteredItems;
            }

            JSONObject items = body.optJSONObject("items");
            if (items == null) {
                System.err.println("WeatherDataParser> No items in response.");
                return filteredItems;
            }

            JSONArray itemArray = items.optJSONArray("item");
            if (itemArray == null || itemArray.length() == 0) {
                System.err.println("WeatherDataParser> No item array found in response.");
                return filteredItems;
            }

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                String category = item.optString("category", "N/A");
                if (getRequiredCategories(apiType).contains(category)) {
                    switch (apiType) {
                        case "ULTRA_SRT_NCST" -> filteredItems.add(createUltraSrtNcstWeatherItem(item));
                        case "VILAGE_FCST" -> filteredItems.add(createVilageFcstWeatherItem(item));
                        case "ULTRA_SRT_FCST" -> filteredItems.add(createUlterSrtFcstWeatherItem(item));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("WeatherDataParser> Error while parsing JSON: " + e.getMessage());
        }
        return filteredItems;
    }


    private WeatherResponse.Item createUltraSrtNcstWeatherItem(JSONObject item) {
        WeatherResponse.Item weatherItem = new WeatherResponse.Item();
        weatherItem.setBaseDate(item.optString("baseDate", "N/A"));
        weatherItem.setBaseTime(item.optString("baseTime", "N/A"));
        weatherItem.setCategory(item.optString("category", "N/A"));
        weatherItem.setNx(item.optInt("nx", 0));
        weatherItem.setNy(item.optInt("ny", 0));
        weatherItem.setObsrValue(item.optFloat("obsrValue", 0));
        return weatherItem;
    }

    private WeatherResponse.Item createVilageFcstWeatherItem(JSONObject item) {
        WeatherResponse.Item weatherItem = new WeatherResponse.Item();
        weatherItem.setBaseDate(item.optString("baseDate", "N/A")); // 기준 날짜
        weatherItem.setBaseTime(item.optString("baseTime", "N/A")); // 기준 시간
        weatherItem.setCategory(item.optString("category", "N/A")); // 데이터 유형
        weatherItem.setFcstDate(item.optString("fcstDate", "N/A")); // 예보 날짜
        weatherItem.setFcstTime(item.optString("fcstTime", "N/A")); // 예보 시간
        weatherItem.setNx(item.optInt("nx", 0)); // X 좌표
        weatherItem.setNy(item.optInt("ny", 0)); // Y 좌표
        weatherItem.setFcstValue(item.optFloat("fcstValue", 0)); // 예보 값
        return weatherItem;
    }

    private  WeatherResponse.Item createUlterSrtFcstWeatherItem(JSONObject item) {
        WeatherResponse.Item weatherItem = new WeatherResponse.Item();
        weatherItem.setBaseDate(item.optString("baseDate", "N/A")); // 기준 날짜
        weatherItem.setBaseTime(item.optString("baseTime", "N/A")); // 기준 시간
        weatherItem.setCategory(item.optString("category", "N/A")); // 데이터 유형
        weatherItem.setFcstDate(item.optString("fcstDate", "N/A")); // 예보 날짜
        weatherItem.setFcstTime(item.optString("fcstTime", "N/A")); // 예보 시간
        weatherItem.setNx(item.optInt("nx", 0)); // X 좌표
        weatherItem.setNy(item.optInt("ny", 0)); // Y 좌표
        weatherItem.setFcstValue(item.optFloat("fcstValue", 0)); // 예보 값
        return weatherItem;
    }

    private Set<String> getRequiredCategories(String apiType) {
        return switch (apiType) {
            case "ULTRA_SRT_NCST" -> ULTRA_SRT_NCST_CATEGORIES;
            case "ULTRA_SRT_FCST" -> ULTRA_SRT_FCST_CATEGORIES;
            case "VILAGE_FCST" -> VILAGE_FCST_CATEGORIES;
            default -> throw new IllegalArgumentException("Invalid API type: " + apiType);
        };
    }
}
