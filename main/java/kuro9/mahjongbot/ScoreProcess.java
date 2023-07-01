package kuro9.mahjongbot;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import kuro9.mahjongbot.data.UserGameData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.IntStream;

/**
 * 게임 데이터를 처리하기 위한 핸들러 클래스입니다.
 */
@Deprecated(forRemoval = true)
public class ScoreProcess {

    /**
     * sunwi.csv 파일에 새 반장전 결과를 추가합니다. 인덱스와 추가된 날짜가 같이 기록됩니다.
     * <br>
     * 매개변수에서 중복된 이름이 있는지, 점수의 합이 10만점인지, 점수가 정렬되어 있는지 검사합니다.
     *
     * @param name  1위부터 4위의 이름이 기록된 {@code String} 배열
     * @param score 1위부터 4위의 점수가 기록된 {@code int} 배열
     * @return 입력된 결과의 인덱스 (매개변수에 대한 논리적 에러 : -1, {@link IOException} : -2)
     */
    public static int addScore(String[] name, int[] score) {
        if (
                (Arrays.stream(name).distinct().count() != 4) || (Arrays.stream(score).sum() != 100000) ||
                        !((score[0] >= score[1]) && (score[1] >= score[2]) && (score[2] >= score[3]))
        ) return -1;

        long line_count;
        try {
            line_count = Files.lines(Paths.get(Setting.DATA_PATH)).count();
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            CSVWriter csv = new CSVWriter(new FileWriter(Setting.DATA_PATH, true));

            String[] str = new String[10];
            str[0] = String.valueOf(line_count);
            str[1] = date;
            int count = 2;
            for (int i = 0; i < 4; i++) {
                str[count++] = name[i];
                str[count++] = String.valueOf(score[i]);
            }

            csv.writeNext(str);
            csv.close();
        }
        catch (IOException e) {
            return -2;
        }
        return (int) line_count;
    }

    /**
     * 데이터를 미리 유저 객체로 처리해 두어 효율성을 높입니다.
     */
    public static void revalidData() {
        LocalDate now = LocalDate.now();
        ObjectOutputStream ostream;
        int month = now.getMonthValue();
        int year = now.getYear();
        try {
            ostream = new ObjectOutputStream(new FileOutputStream(Setting.USERDATA_PATH));
            ostream.writeObject(getUserDataList());
            ostream.close();

            ostream = new ObjectOutputStream(new FileOutputStream(Setting.getValidMonthDataPath()));
            ostream.writeObject(getUserDataList(month, year));
            ostream.close();

            ostream = new ObjectOutputStream(new FileOutputStream(Setting.getValidHalfDataPath()));
            ostream.writeObject(getUserDataList((((month - 1) / 6) + 1), year, month, year));
            ostream.close();
        }
        catch (IOException e) {
            Logger.addSystemErrorEvent("revalid-io-err");
        }
    }

    /**
     * 매개변수로 받은 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @param start_month  시작 월
     * @param start_year   시작 년도
     * @param end_month    끝 월
     * @param end_year     끝 년도
     * @param isdatesearch 코드 재사용을 위한 내부 매개변수
     * @return {@link kuro9.mahjongbot.data.UserGameData}     형 유저 데이터 리스트
     */
    private static HashMap<String, kuro9.mahjongbot.data.UserGameData> processUserData(int start_month, int start_year, int end_month, int end_year, boolean isdatesearch) {
        HashMap<String, kuro9.mahjongbot.data.UserGameData> uma_table = new HashMap<>();
        try {
            CSVReader csv = new CSVReader(new FileReader(Setting.DATA_PATH));
            csv.skip(1);
            for (var line : csv.readAll()) {
                int search_year = Integer.parseInt(line[1].split("\\.")[0]);
                int search_month = Integer.parseInt(line[1].split("\\.")[1]);
                if (!isdatesearch || ((start_year <= search_year && search_year <= end_year) && (start_month <= search_month && search_month <= end_month))) {
                    for (int i = 2; i < 10; i += 2) {
                        if (!uma_table.containsKey(line[i])) {
                            kuro9.mahjongbot.data.UserGameData user = new kuro9.mahjongbot.data.UserGameData(line[i]);
                            user.addGameData(Integer.parseInt(line[i + 1]), (i / 2));
                            uma_table.put(line[i], user);
                        }
                        else {
                            uma_table.get(line[i]).addGameData(Integer.parseInt(line[i + 1]), (i / 2));
                        }
                    }
                }
            }

        }
        catch (IOException | CsvException e) {
            Logger.addSystemErrorEvent("process-data-io-err");
        }

        return uma_table;
    }

    /**
     * 전체 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @return {@link kuro9.mahjongbot.data.UserGameData} 형 유저 데이터 리스트
     */
    public static HashMap<String, kuro9.mahjongbot.data.UserGameData> getUserDataList() {
        return processUserData(0, 0, 0, 0, false);
    }

    /**
     * 매개변수로 받은 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @param month 검색할 월
     * @param year  검색할 년도
     * @return {@link kuro9.mahjongbot.data.UserGameData} 형 유저 데이터 리스트
     */
    public static HashMap<String, kuro9.mahjongbot.data.UserGameData> getUserDataList(int month, int year) {
        return processUserData(month, year, month, year, true);
    }

    /**
     * 매개변수로 받은 기간 동안의 유저 데이터 리스트를 반환합니다.
     *
     * @param start_month 시작 월
     * @param start_year  시작 년도
     * @param end_month   끝 월
     * @param end_year    끝 년도
     * @return {@link kuro9.mahjongbot.data.UserGameData}     형 유저 데이터 리스트
     */
    public static HashMap<String, UserGameData> getUserDataList(int start_month, int start_year, int end_month, int end_year) {
        return processUserData(start_month, start_year, end_month, end_year, true);
    }

    private static int[][] recentGameResult(String name, int start_month, int start_year, int end_month, int end_year, boolean isdatesearch) {
        Queue<Integer> queue = new LinkedList<>();
        try {
            CSVReader csv = new CSVReader(new FileReader(Setting.DATA_PATH));
            csv.skip(1);
            for (var line : csv.readAll()) {
                int search_year = Integer.parseInt(line[1].split("\\.")[0]);
                int search_month = Integer.parseInt(line[1].split("\\.")[1]);
                if (!isdatesearch || ((start_year <= search_year && search_year <= end_year) && (start_month <= search_month && search_month <= end_month))) {
                    for (int i = 2; i < 10; i += 2) {
                        // 등수 : i/2, 점수: line[i+1]
                        if (line[i].equals(name))
                            queue.offer((i * 5) + (Integer.parseInt(line[i + 1]) >= 50000 ? 1 : 0));
                    }

                    while (queue.size() > 10) queue.poll();
                }

            }

        }
        catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        IntStream.generate(() -> 0).limit(10 - queue.size()).forEach(queue::add);

        return new int[][]{
                queue.stream().mapToInt(i -> i / 10).toArray(),
                queue.stream().mapToInt(i -> i % 10).toArray()
        };
    }

    /**
     * 그래프 작성용 메소드입니다. 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param name 검색할 유저의 이름입니다.
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     */
    public static int[][] recentGameResult(String name) {
        return recentGameResult(name, 0, 0, 0, 0, false);
    }

    /**
     * 그래프 작성용 메소드입니다. 검색 범위 내 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param name  검색할 유저의 이름입니다.
     * @param month 검색할 월
     * @param year  검색할 년도
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     */
    public static int[][] recentGameResult(String name, int month, int year) {
        return recentGameResult(name, month, year, month, year, true);
    }

    /**
     * 그래프 작성용 메소드입니다. 검색 범위 내 최근 10국의 순위와 냥글라스 여부를 이차원 배열로 반환합니다.
     *
     * @param name        검색할 유저의 이름입니다.
     * @param start_month 시작 월
     * @param start_year  시작 년도
     * @param end_month   끝 월
     * @param end_year    끝 년도
     * @return [0][] : 최근 10국의 순위(범위 : [1, 4]), [1][] : 냥글라스 여부(범위 : [0, 1])
     */
    public static int[][] recentGameResult(String name, int start_month, int start_year, int end_month, int end_year) {
        return recentGameResult(name, start_month, start_year, end_month, end_year, true);
    }
}
