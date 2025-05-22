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
 * 天気予報アプリ
 * このアプリケーションは、気象庁のWeb APIから大阪府の天気予報データを取得し、表示します。
 * 
 * org.jsonライブラリを使用するために、依存関係をプロジェクトに追加する必要があります。
 * 
 * @author n.katayama
 * @version 1.0
 */
// 天気データ取得用クラス
class WeatherDataFetcher {
    // 指定URLから天気データ(JSON)を取得
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
            throw new IOException("データ取得に失敗しました。レスポンスコード: " + responseCode);
        }
    }
}

// JSONデータ解析用クラス
class WeatherDataParser {
    // 天気JSONデータを解析し、日付・天気・風速・降水確率のリストを返す
    public List<String[]> parseWeatherData(String jsonData) {
        JSONArray rootArray = new JSONArray(jsonData);
        // 天気・風
        JSONObject weatherObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(0);
        // 降水確率
        JSONObject popObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(1);

        List<String[]> weatherInfo = new ArrayList<>();
        JSONArray timeDefinesArray = weatherObject.getJSONArray("timeDefines");
        JSONArray weathersArray = weatherObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("weathers");

        JSONArray windsArray = null;
        if (weatherObject.getJSONArray("areas").getJSONObject(0).has("winds")) {
            windsArray = weatherObject.getJSONArray("areas").getJSONObject(0).getJSONArray("winds");
        }

        JSONArray popsArray = popObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("pops");

        for (int i = 0; i < timeDefinesArray.length(); i++) {
            String wind = (windsArray != null && i < windsArray.length()) ? windsArray.getString(i) : "-";
            String pop = (i < popsArray.length() && !popsArray.isNull(i) && !popsArray.getString(i).isEmpty())
                    ? popsArray.getString(i) + "%" : "--";
            weatherInfo.add(new String[] {
                    timeDefinesArray.getString(i),
                    weathersArray.getString(i),
                    wind,
                    pop
            });
        }
        return weatherInfo;
    }
}

// 天気データ表示用クラス
class WeatherDataPrinter {
    // 解析した天気データをコンソールに出力
    public void printWeatherData(List<String[]> weatherInfo) {
        System.out.println("日付        天気    風速    降水確率");
        for (String[] info : weatherInfo) {
            LocalDateTime dateTime = LocalDateTime.parse(info[0], DateTimeFormatter.ISO_DATE_TIME);
            String youbi = dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
            System.out.println(
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi + "） "
                    + info[1] + "    " + info[2] + "    " + info[3]);
        }
    }
}

// メイン処理クラス
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
