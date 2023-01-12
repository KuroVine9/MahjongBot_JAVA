package kuro9.mahjongbot;

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
    public static String PATH;
    public static String LOG_PATH;
    public static String ERROR_LOG_PATH;
    public static String TOKEN_PATH;
    public static String USERDATA_PATH;
    public static String MONTH_USERDATA_PATH;
    public static String GRAPH_PATH;
    public static String GRAPH_NAME;
    public static String INST_PATH;

    public static void init() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/main/resources/settings/setting.json"));
        } catch (IOException | ParseException e) {
            Logger.addSystemErrorEvent("setting-parse-err", null);
            throw new RuntimeException(e);
        }
        JSONObject jsonObject = (JSONObject) obj;
        RETURN_POINT = ((Long) jsonObject.get("RETURN_POINT")).intValue();
        ADMIN_ID = (Long) jsonObject.get("ADMIN_ID");
        PATH = (String) jsonObject.get("PATH");
        LOG_PATH = (String) jsonObject.get("LOG_PATH");
        ERROR_LOG_PATH = (String) jsonObject.get("ERROR_LOG_PATH");
        TOKEN_PATH = (String) jsonObject.get("TOKEN_PATH");
        USERDATA_PATH = (String) jsonObject.get("USERDATA_PATH");
        MONTH_USERDATA_PATH = jsonObject.get("MONTH_USERDATA_PATH").toString();
        GRAPH_PATH = (String) jsonObject.get("GRAPH_PATH");
        INST_PATH = jsonObject.get("INST_PATH").toString();

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
                String.format("%d%d", month, year)
        );
    }

}
