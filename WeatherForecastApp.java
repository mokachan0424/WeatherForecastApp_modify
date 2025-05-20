import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 天気アプリ - 本体
 * このアプリケーションは、気象庁のWeb APIから大阪府の天気予報データを取得して表示します
 * 
 * @author n.katayama
 * @version 1.0
 */
// WeatherDataFetcher クラス: 天気データを取得する
class WeatherDataFetcher {
    public String fetchWeatherData(String targetUrl) throws IOException, URISyntaxException {
        URI uri = new URI(targetUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBody.append(line);
                }
            }
            return responseBody.toString();
        } else {
            throw new IOException("Failed to fetch data. Response code: " + responseCode);
        }
    }
}

// WeatherDataParser クラス: JSONデータを解析する
class WeatherDataParser {
    public List<String[]> parseWeatherData(String jsonData) {
        JSONArray rootArray = new JSONArray(jsonData);
        JSONObject timeStringObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(0);

        List<String[]> weatherInfo = new ArrayList<>();
        JSONArray timeDefinesArray = timeStringObject.getJSONArray("timeDefines");
        JSONArray weathersArray = timeStringObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("weathers");

        for (int i = 0; i < timeDefinesArray.length(); i++) {
            weatherInfo.add(new String[] {
                    timeDefinesArray.getString(i),
                    weathersArray.getString(i)
            });
        }
        return weatherInfo;
    }
}

// WeatherDataPrinter クラス: データを表示する
class WeatherDataPrinter {
    public void printWeatherData(List<String[]> weatherInfo) {
        for (String[] info : weatherInfo) {
            LocalDateTime dateTime = LocalDateTime.parse(info[0], DateTimeFormatter.ISO_DATE_TIME);
            String youbi = dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
            System.out.println(
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi + "） " + info[1]);
        }
    }
}

// WeatherForecastApp クラス: メイン処理
public class WeatherForecastApp {
    private static final String TARGET_URL = "https://www.jma.go.jp/bosai/forecast/data/forecast/270000.json";

    public static void main(String[] args) {
        WeatherDataFetcher fetcher = new WeatherDataFetcher();
        WeatherDataParser parser = new WeatherDataParser();
        WeatherDataPrinter printer = new WeatherDataPrinter();

        try {
            String jsonData = fetcher.fetchWeatherData(TARGET_URL);
            List<String[]> weatherInfo = parser.parseWeatherData(jsonData);
            printer.printWeatherData(weatherInfo);
        } catch (IOException | URISyntaxException e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }
    }
}
