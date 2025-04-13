import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class History {

    private static void getHistoryData(String token, String figi) {

        try {

            URL url = new URL("https://invest-public-api.tinkoff.ru/history-data?figi=" + figi + "&year=2022");

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod("GET");
            httpsURLConnection.setConnectTimeout(1000);
            httpsURLConnection.setDoOutput(false);
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);

            String type = httpsURLConnection.getContentType();

            int result = httpsURLConnection.getResponseCode();
            if(result == 200) {

                File file = new File(figi + "_2022.zip");

                OutputStream output = new FileOutputStream(file, false);

                httpsURLConnection.getInputStream().transferTo(output);

                output.close();
            }

            System.out.println("Code: " + result);

        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {

        String token = "token_tinkoff.txt";

        if(args.length == 1)
            token = args[0];

        try {
            token = Files.readString(Paths.get(token), StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }

        getHistoryData(token, "BBG000BBJQV0");
    }
}
