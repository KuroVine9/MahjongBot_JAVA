package kuro9.mahjongbot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public record Setting() {
    static int RETURN_POINT;
    static int[] UMA;
    static long ADMIN_ID;
    static String PATH;
    static String LOG_PATH;
    static String TOKEN_PATH;

    public static void init() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("src/main/resources/setting.json"));
            JSONObject jsonObject = (JSONObject) obj;
            RETURN_POINT = ((Long) jsonObject.get("RETURN_POINT")).intValue();
            ADMIN_ID = (Long) jsonObject.get("ADMIN_ID");
            PATH = (String) jsonObject.get("PATH");
            LOG_PATH = (String) jsonObject.get("LOG_PATH");
            TOKEN_PATH = (String) jsonObject.get("TOKEN_PATH");
            JSONArray jsonArray = (JSONArray) jsonObject.get("UMA");
            UMA = new int[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                UMA[i] = ((Long) jsonArray.get(i)).intValue();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}
