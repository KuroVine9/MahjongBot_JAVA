package kuro9.mahjongbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Setting() {
    public static int RETURN_POINT;
    public static int[] UMA;
    public static long ADMIN_ID;
    public static RestAction<User> ADMIN;
    public static String DATA_PATH;
    public static String LOG_PATH;
    public static String ERROR_LOG_PATH;
    public static String TOKEN_PATH;
    public static String USERDATA_PATH;
    public static String MONTH_USERDATA_PATH;
    public static String HALF_USERDATA_PATH;
    public static String GRAPH_PATH;
    public static String GRAPH_NAME;
    public static String INST_PATH;
    public static String CREDENTIAL_PATH;
    public static String DATA_FILE_ID;
    public static String LOG_FILE_ID;
    public static String ERROR_LOG_FILE_ID;
    public static String IMAGE_BACKGROUND_PATH;
    public static String IMAGE_NYANGLASS_PATH;
    public static String MAHJONG_BASE_PATH;
    public static String FALLBACK_GRAPH_PATH;
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;
    public static JDA JDA;

    public static void init(String token) {
        JDA = JDABuilder.createDefault(token).build();
    }

    public static void parseString() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("resources/setting.json"));
        }
        catch (IOException | ParseException e) {
            Logger.addSystemErrorEvent(Logger.SETTING_JSON_PARSE_ERR);
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = (JSONObject) obj;
        RETURN_POINT = Integer.parseInt(jsonObject.get("RETURN_POINT").toString());
        ADMIN_ID = Long.parseLong(jsonObject.get("ADMIN_ID").toString());
        DATA_PATH = jsonObject.get("DATA_PATH").toString();
        LOG_PATH = jsonObject.get("LOG_PATH").toString();
        ERROR_LOG_PATH = jsonObject.get("ERROR_LOG_PATH").toString();
        TOKEN_PATH = jsonObject.get("TOKEN_PATH").toString();
        USERDATA_PATH = jsonObject.get("USERDATA_PATH").toString();
        MONTH_USERDATA_PATH = jsonObject.get("MONTH_USERDATA_PATH").toString();
        HALF_USERDATA_PATH = jsonObject.get("HALF_USERDATA_PATH").toString();
        GRAPH_PATH = jsonObject.get("GRAPH_PATH").toString();
        INST_PATH = jsonObject.get("INST_PATH").toString();
        CREDENTIAL_PATH = jsonObject.get("CREDENTIAL_PATH").toString();
        DATA_FILE_ID = jsonObject.get("DATA_FILE_ID").toString();
        LOG_FILE_ID = jsonObject.get("LOG_FILE_ID").toString();
        ERROR_LOG_FILE_ID = jsonObject.get("ERROR_LOG_FILE_ID").toString();
        IMAGE_BACKGROUND_PATH = jsonObject.get("IMAGE_BACKGROUND_PATH").toString();
        IMAGE_NYANGLASS_PATH = jsonObject.get("IMAGE_NYANGLASS_PATH").toString();
        MAHJONG_BASE_PATH = jsonObject.get("MAHJONG_BASE_PATH").toString();
        FALLBACK_GRAPH_PATH = jsonObject.get("FALLBACK_GRAPH_PATH").toString();
        DB_URL = jsonObject.get("DB_URL").toString();
        DB_USER = jsonObject.get("DB_USER").toString();
        DB_PASSWORD = jsonObject.get("DB_PASSWORD").toString();

        JSONArray jsonArray = (JSONArray) jsonObject.get("UMA");
        UMA = new int[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            UMA[i] = ((Long) jsonArray.get(i)).intValue();
        }

        String[] test = GRAPH_PATH.split("/");
        GRAPH_NAME = test[test.length - 1];
    }

    public static String getValidMonthDataPath() {
        return MONTH_USERDATA_PATH.replace(
                "YYYYMM",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
        );
    }

    public static String getValidMonthDataPath(int month, int year) {
        return MONTH_USERDATA_PATH.replace(
                "YYYYMM",
                String.format("%d%d", year, month)
        );
    }

    public static String getValidHalfDataPath() {
        return HALF_USERDATA_PATH.replace(
                "YYYYHH",
                String.format(
                        "%sH%d",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")),
                        ((LocalDateTime.now().getMonthValue() - 1) / 6) + 1
                )
        );
    }

    public static String getValidHalfDataPath(int half, int year) {
        return HALF_USERDATA_PATH.replace(
                "YYYYHH",
                String.format("%dH%d", year, half)
        );
    }

    public static void setAdmin(RestAction<User> ADMIN) {
        Setting.ADMIN = ADMIN;
    }
}
