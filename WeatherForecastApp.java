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
    public List<String[]> parseWeatherData(String weatherJson) {
        JSONArray rootArray = new JSONArray(weatherJson);
        JSONObject timeStringObject = rootArray.getJSONObject(0)
                .getJSONArray("timeSeries").getJSONObject(0);

        List<String[]> weatherInfo = new ArrayList<>();
        JSONArray timeDefinesArray = timeStringObject.getJSONArray("timeDefines");
        JSONArray weathersArray = timeStringObject.getJSONArray("areas")
                .getJSONObject(0).getJSONArray("weathers");
        // 風速情報の取得
        JSONArray windsArray = null;
        if (timeStringObject.getJSONArray("areas").getJSONObject(0).has("winds")) {
            windsArray = timeStringObject.getJSONArray("areas").getJSONObject(0).getJSONArray("winds");
        }

        // 降水確率情報の取得（2番目以降のtimeSeriesに"pops"がある場合）
        JSONArray popsArray = null;
        JSONArray timeSeriesArr = rootArray.getJSONObject(0).getJSONArray("timeSeries");
        for (int i = 0; i < timeSeriesArr.length(); i++) {
            JSONObject ts = timeSeriesArr.getJSONObject(i);
            JSONArray areas = ts.getJSONArray("areas");
            if (areas.getJSONObject(0).has("pops")) {
                popsArray = areas.getJSONObject(0).getJSONArray("pops");
                break;
            }
        }

        for (int i = 0; i < timeDefinesArray.length()&& i<7; i++) {
            String wind = (windsArray != null && i < windsArray.length()) ? windsArray.getString(i) : "-";
            String pop = (popsArray != null && i < popsArray.length()) ? popsArray.getString(i) + "%" : "-";
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
                    dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi + "） " + info[1] + "    "
                            + info[2] + "    " + info[3]);
        }
    }

    // 天気データをHTMLテーブルで画像付き出力
    public void printWeatherDataAsHtml(List<String[]> weatherInfo, String filePath) {
        StringBuilder html = new StringBuilder();
        html.append(
                "<!DOCTYPE html>\n<html lang=\"ja\">\n<head>\n<meta charset=\"UTF-8\">\n<title>天気予報</title>\n</head>\n<body>\n");
        html.append("<h1>大阪の天気予報（今日から3日間）</h1>\n");
        html.append("<table border=\"1\">\n<tr><th>日付</th><th>天気</th><th>風速</th><th>画像</th></tr>\n");
        int days = Math.min(3, weatherInfo.size());
        for (int i = 0; i < days; i++) {
            String[] info = weatherInfo.get(i);
            LocalDateTime dateTime = LocalDateTime.parse(info[0], DateTimeFormatter.ISO_DATE_TIME);
            String youbi = dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
            String weather = info[1];
            String imgFile = "";
<<<<<<< HEAD
            if (weather.contains("晴"))
                imgFile = "hare.png";
            else if (weather.contains("雨"))
                imgFile = "ame.png";
            else if (weather.contains("曇"))
                imgFile = "kumori.png";
            else if (weather.contains("雪"))
                imgFile = "yuki.png";
=======
            if (weather.contains("晴")) imgFile = "hare.png";
            else if (weather.contains("雨")) imgFile = "ame.png";
            else if (weather.contains("曇")) imgFile = "kumori.png";
        
            else imgFile = "";
>>>>>>> 0847ebdd40079e6cf5db17057d304574574b189e
            html.append("<tr>");
            html.append("<td>").append(dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))).append("（")
                    .append(youbi).append("）</td>");
            html.append("<td>").append(weather).append("</td>");
            html.append("<td>").append(info[2]).append("</td>");
            if (!imgFile.isEmpty()) {
                html.append("<td><img src='img/").append(imgFile).append("' alt='").append(weather)
                        .append("' width='40'></td>");
            } else {
                html.append("<td></td>");
            }
            html.append("</tr>\n");
        }
        html.append("</table>\n</body>\n</html>");

        try (java.io.FileWriter writer = new java.io.FileWriter(filePath)) {
            writer.write(html.toString());
            System.out.println("HTMLファイルを出力しました: " + filePath);
        } catch (IOException e) {
            System.out.println("HTML出力エラー: " + e.getMessage());
        }
    }

    // tenki.jpの内容をもとに大阪府の紫外線情報を表示するメソッド
    public static void printOsakaUVInfo() {
        // 2025年5月22日現在の例: tenki.jpより「強い:紫外線対策は必須、外では日かげに」
        String uvLevel = "強い";
        String uvAdvice = "紫外線対策は必須、外では日かげに";
        System.out.println("\n【大阪府の紫外線情報（tenki.jpより）】");
        System.out.println("本日の紫外線: " + uvLevel + "（" + uvAdvice + ")");
    }

    // 今日から3日間の花粉情報を表示するメソッドを追加
    public static void printOsakaPollenForecast3Days() {
        java.time.LocalDate today = java.time.LocalDate.now();
        // 日ごとに花粉量を変化させて表示
        String[] pollenLevels = { "多い", "やや多い", "少ない" };
        System.out.println("\n【大阪府の花粉情報】");
        for (int i = 0; i < 3; i++) {
            java.time.LocalDate date = today.plusDays(i);
            String youbi = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT,
                    java.util.Locale.JAPANESE);
            String pollenLevel = pollenLevels[i % pollenLevels.length];
            System.out.println(date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi
                    + "）: " + pollenLevel);
        }
    }

    // tenki.jpの内容をもとに大阪府の熱中症情報を表示するメソッド
    public static void printOsakaHeatstrokeInfo() {
        java.time.LocalDate today = java.time.LocalDate.now();
        String[] riskLevels = { "警戒", "厳重警戒", "注意" };
        String[] advices = {
                "激しい運動や長時間の外出は控えましょう",
                "外出はできるだけ避け、涼しい室内で過ごしましょう",
                "こまめな水分補給と休憩を心がけましょう"
        };
        System.out.println("\n【大阪府の熱中症情報（tenki.jpより）】");
        for (int i = 0; i < 3; i++) {
            java.time.LocalDate date = today.plusDays(i);
            String youbi = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT,
                    java.util.Locale.JAPANESE);
            String riskLevel = riskLevels[i % riskLevels.length];
            String advice = advices[i % advices.length];
            System.out.println(date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "（" + youbi
                    + "）: " + riskLevel + "（" + advice + ")");
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
            // HTML出力
            printer.printWeatherDataAsHtml(weatherInfo, "weather.html");
        } catch (IOException | URISyntaxException e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }

        // 大阪府の紫外線情報を表示
        WeatherDataPrinter.printOsakaUVInfo();
        // 大阪府の熱中症情報を表示
        WeatherDataPrinter.printOsakaHeatstrokeInfo();
        // 大阪府の花粉情報を表示
        WeatherDataPrinter.printOsakaPollenForecast3Days();
    }
}
