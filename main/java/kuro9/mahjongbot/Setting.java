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
    public static String FILE_ID;
    public static String IMAGE_BACKGROUND_PATH;
    public static String IMAGE_NYANGLASS_PATH;

    public static void init() {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(new FileReader("src/main/resources/setting.json"));
        } catch (IOException | ParseException e) {
            Logger.addSystemErrorEvent("setting-parse-err", null);
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
        FILE_ID = jsonObject.get("FILE_ID").toString();
        IMAGE_BACKGROUND_PATH = jsonObject.get("IMAGE_BACKGROUND_PATH").toString();
        IMAGE_NYANGLASS_PATH = jsonObject.get("IMAGE_NYANGLASS_PATH").toString();


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
}
