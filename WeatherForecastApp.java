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
    // 天気JSONデータを解析し、日付・天気・風速情報のリストを返す
    public List<String[]> parseWeatherData(String jsonData) {
        JSONArray rootArray = new JSONArray(jsonData);
        JSONObject timeStringObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(0);

        List<String[]> weatherInfo = new ArrayList<>();
        JSONArray timeDefinesArray = timeStringObject.getJSONArray("timeDefines");
        JSONArray weathersArray = timeStringObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("weathers");
        // 風速情報の取得（仮: 1番目の timeSeries 配列の areas 内の winds 配列と仮定）
        JSONArray windsArray = null;
        if (timeStringObject.getJSONArray("areas").getJSONObject(0).has("winds")) {
            windsArray = timeStringObject.getJSONArray("areas").getJSONObject(0).getJSONArray("winds");
        }

        for (int i = 0; i < timeDefinesArray.length(); i++) {
            String wind = (windsArray != null && i < windsArray.length()) ? windsArray.getString(i) : "-";
            weatherInfo.add(new String[] {
                    timeDefinesArray.getString(i),
                    weathersArray.getString(i),
                    wind
            });
        }
        return weatherInfo;
    }
}

// 天気データ表示用クラス
class WeatherDataPrinter {
    // 解析した天気データをコンソールに出力
    // 各データは日付、天気、風速の順で表示されます
    public void printWeatherData(List<String[]> weatherInfo) {
        System.out.println("日付        天気    風速"); // ヘッダー行を表示
        for (String[] info : weatherInfo) {
            LocalDateTime dateTime = LocalDateTime.parse(info[0], DateTimeFormatter.ISO_DATE_TIME);
            String youbi = dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE); // 曜日を日本語で取得
            System.out.println(
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi + "） " + info[1] + "    "
                            + info[2]); // 日付、天気、風速をフォーマットして表示
        }
    }
}

// メイン処理クラス
public class WeatherForecastApp {
    private static final String TARGET_URL = "https://www.jma.go.jp/bosai/forecast/data/forecast/270000.json"; // 気象庁の天気予報APIのURL

    public static void main(String[] args) {
        WeatherDataFetcher fetcher = new WeatherDataFetcher(); // 天気データ取得用クラスのインスタンスを作成
        WeatherDataParser parser = new WeatherDataParser(); // JSONデータ解析用クラスのインスタンスを作成
        WeatherDataPrinter printer = new WeatherDataPrinter(); // 天気データ表示用クラスのインスタンスを作成

        try {
            String jsonData = fetcher.fetchWeatherData(TARGET_URL); // 天気データを取得
            List<String[]> weatherInfo = parser.parseWeatherData(jsonData); // JSONデータを解析
            printer.printWeatherData(weatherInfo); // 解析結果を表示
        } catch (IOException | URISyntaxException e) {
            System.out.println("エラーが発生しました: " + e.getMessage()); // エラー発生時のメッセージを表示
        }
    }
}
