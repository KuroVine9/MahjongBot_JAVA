package kuro9.mahjongbot;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreProcess {

    /**
     * 사용하는 csv 파일의 경로
     */
    private final String PATH;

    /**
     * 반환점
     */
    private final int return_point = 25000;

    /**
     * 1등-4등에게 더해지는 우마
     */
    private final int[] uma = new int[]{15, 5, -5, -15};

    public ScoreProcess(String PATH) {
        this.PATH = PATH;
        UserGameData.uma = this.uma;
        UserGameData.return_point = this.return_point;
    }

    /**
     * sunwi.csv 파일에 새 반장전 결과를 추가합니다. 인덱스와 추가된 날짜가 같이 기록됩니다.
     * <br>
     * 매개변수에서 중복된 이름이 있는지, 점수의 합이 10만점인지, 점수가 정렬되어 있는지 검사합니다.
     *
     * @param name  1위부터 4위의 이름이 기록된 {@code String} 배열
     * @param score 1위부터 4위의 점수가 기록된 {@code int} 배열
     * @return 입력된 결과의 인덱스 (매개변수에 대한 논리적 에러 : -1, {@link IOException} : -2)
     */
    public int addScore(String[] name, int[] score) {
        if (
                (Arrays.stream(name).distinct().count() != 4) || (Arrays.stream(score).sum() != 100000) ||
                        !((score[0] >= score[1]) && (score[1] >= score[2]) && (score[2] >= score[3]) && (score[3] >= score[4]))
        ) return -1;

        long line_count;
        try {
            line_count = Files.lines(Paths.get(PATH)).count() + 1;
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            CSVWriter csv = new CSVWriter(new FileWriter(PATH, true));

            String[] str = new String[10];
            str[0] = String.valueOf(line_count);
            str[1] = date;
            int count = 2;
            for (int i = 0; i < 4; i++) {
                str[count++] = name[i].replaceAll(" ", "");
                str[count++] = String.valueOf(score[i]);
            }

            csv.writeNext(str);
            csv.close();
        } catch (IOException e) {
            return -2;
        }
        return (int) line_count;
    }

    /**
     * 매개변수로 받은 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @param month        검색할 월
     * @param year         검색할 년도
     * @param isdatesearch 코드 재사용을 위한 내부 매개변수
     * @return {@link UserGameData} 형 유저 데이터 리스트
     */
    private List<UserGameData> processUserData(int month, int year, boolean isdatesearch) {
        HashMap<String, UserGameData> uma_table = new HashMap<>();
        try {
            CSVReader csv = new CSVReader(new FileReader(PATH));
            for (var line : csv.readAll()) {
                if ((Integer.parseInt(line[1].split("\\.")[0]) != year
                        || Integer.parseInt(line[1].split("\\.")[1]) != month) && isdatesearch) continue;

                for (int i = 2; i < 10; i += 2) {
                    if (!uma_table.containsKey(line[i])) {
                        UserGameData user = new UserGameData(line[i]);
                        user.addGameData(Integer.parseInt(line[i + 1]), (i / 2));
                        uma_table.put(line[i], user);
                    }
                    else {
//                        uma_table.put(
//                                line[i],
//                                Math.round(
//                                    (uma_table.get(line[i]) + scoreToUma(Integer.parseInt(line[i + 1]), i / 2)) * 10
//                                ) / 10.0
//                        );
                        uma_table.get(line[i]).addGameData(Integer.parseInt(line[i + 1]), (i / 2));
                    }
                }

            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        return uma_table.entrySet().stream().peek(
                k -> k.getValue().updateAllData()
        ).map(
                Map.Entry::getValue
        ).toList();
    }

    /**
     * 전체 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @return {@link UserGameData} 형 유저 데이터 리스트
     */
    public List<UserGameData> getUserDataList() {
        return processUserData(0, 0, false);
    }

    /**
     * 매개변수로 받은 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @param month 검색할 월
     * @param year  검색할 년도
     * @return {@link UserGameData} 형 유저 데이터 리스트
     */
    public List<UserGameData> getUserDataList(int month, int year) {
        return processUserData(month, year, true);
    }

    /**
     * 일정 기간 내 {@code filter}국 이상의 유저만 집계한 유저 데이터 리스트를 반환합니다.
     *
     * @param month  검색할 월
     * @param year   검색할 년도
     * @param filter 기준 국 수
     * @return {@link UserGameData} 형 유저 데이터 리스트
     */
    public List<UserGameData> getFilteredUserDataList(int month, int year, int filter) {
        return getUserDataList(month, year).stream().filter(
                var -> var.game_count >= filter
        ).toList();
    }

    /**
     * {@code filter}국 이상의 유저만 집계한 유저 데이터 리스트를 반환합니다.
     *
     * @param filter 기준 국 수
     * @return {@link UserGameData} 형 유저 데이터 리스트
     */
    public List<UserGameData> getFilteredUserDataList(int filter) {
        return getUserDataList().stream().filter(
                var -> var.game_count >= filter
        ).toList();
    }

}
