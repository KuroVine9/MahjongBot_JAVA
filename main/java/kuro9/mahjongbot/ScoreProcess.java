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

    /**
     * 점수를 우마로 변환합니다. {@link ScoreProcess} 내 저장되어있는 반환점과 우마를 활용합니다.
     * 소수점 한 자리까지 출력합니다.
     *
     * @param score 변환할 점수
     * @param rank  순위 (범위 : [1, 4])
     * @return {@code double}형 우마 값
     */
    private double scoreToUma(int score, int rank) {
        double jun_uma = ((score - return_point) / 1000.0) + uma[rank - 1];
        return Math.round(jun_uma * 10) / 10.0;
    }

    /**
     * 오버라이딩을 위한 private 메소드
     * <br>
     * 매개변수로 받은 기간 동안의 우마 리스트를 반환합니다.
     *
     * @param month        검색할 월
     * @param year         검색할 년도
     * @param isdatesearch 코드 재사용을 위한 내부 매개변수
     * @return {@code List<String[]>}, String[]의 [0] = 이름, [1] = 우마
     */
    private List<String[]> getUmaList(int month, int year, boolean isdatesearch) {
        HashMap<String, Double> uma_table = new HashMap<>();
        try {
            CSVReader csv = new CSVReader(new FileReader(PATH));
            for (var line : csv.readAll()) {
                if ((Integer.parseInt(line[1].split("\\.")[0]) != year
                        || Integer.parseInt(line[1].split("\\.")[1]) != month) && isdatesearch) continue;

                for (int i = 2; i < 10; i += 2) {
                    if (!uma_table.containsKey(line[i])) {
                        uma_table.put(line[i], scoreToUma(Integer.parseInt(line[i + 1]), i / 2));
                    }
                    else {
                        uma_table.put(line[i], Math.round(
                                (uma_table.get(line[i]) + scoreToUma(Integer.parseInt(line[i + 1]), i / 2)) * 10
                        ) / 10.0);
                    }
                }

            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        return uma_table.entrySet().stream().map(
                val -> new String[]{val.getKey(), val.getValue().toString()}
        ).toList();
    }

    /**
     * 오버라이딩을 위한 private 메소드
     * <br>
     * 일정 기간 내 {@code filter}국 이상 플레이한 유저의 목록을 반환합니다.
     *
     * @param filter       기준 국 수
     * @param month        검색할 월
     * @param year         검색할 년도
     * @param isdatesearch 코드 재사용을 위한 내부 매개변수
     * @return {@code List<String>} 형식의 유저 이름 리스트
     */
    private List<String> getGameCountFilteredList(int filter, int month, int year, boolean isdatesearch) {
        HashMap<String, Integer> count_map = new HashMap<>();
        try {
            CSVReader csv = new CSVReader(new FileReader(PATH));
            for (var line : csv.readAll()) {
                if ((Integer.parseInt(line[1].split("\\.")[0]) != year
                        || Integer.parseInt(line[1].split("\\.")[1]) != month) && isdatesearch) continue;

                for (int i = 2; i < 10; i += 2) {
                    if (!count_map.containsKey(line[i])) {
                        count_map.put(line[i], 1);
                    }
                    else {
                        count_map.put(line[i], (count_map.get(line[i]) + 1));
                    }
                }

            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        return count_map.entrySet().stream().filter(val -> val.getValue() >= filter).map(
                Map.Entry::getKey
        ).toList();
    }

    /**
     * {@code filter}국 이상 플레이한 유저의 목록을 반환합니다.
     *
     * @param filter 기준 국 수
     * @return {@code List<String>} 형식의 유저 이름 리스트
     */
    private List<String> getGameCountFilteredList(int filter) {
        return getGameCountFilteredList(filter, 0, 0, false);
    }

    /**
     * 일정 기간 내 {@code filter}국 이상 플레이한 유저의 목록을 반환합니다.
     *
     * @param filter 기준 국 수
     * @param month  검색할 월
     * @param year   검색할 년도
     * @return {@code List<String>} 형식의 유저 이름 리스트
     */
    private List<String> getGameCountFilteredList(int filter, int month, int year) {
        return getGameCountFilteredList(filter, month, year, true);
    }

    public ScoreProcess(String path) {
        PATH = path;
    }

    /**
     * sunwi.csv 파일에 새 반장전 결과를 추가합니다. 인덱스와 추가된 날짜가 같이 기록됩니다.
     * <br>
     * 매개변수에서 중복된 이름이 있는지, 점수의 합이 10만점인지 검사합니다.
     *
     * @param name  1위부터 4위의 이름이 기록된 {@code String} 배열
     * @param score 1위부터 4위의 점수가 기록된 {@code int} 배열
     * @return 매개변수에 대한 논리적 에러 : 1, {@link IOException} : -1, 이상 없음 : 0
     */
    public int addScore(String[] name, int[] score) {
        if (Arrays.stream(name).distinct().count() != 4 || Arrays.stream(score).sum() != 100000) return 1;

        try {
            long line_count = Files.lines(Paths.get(PATH)).count() + 1;
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            CSVWriter csv = new CSVWriter(new FileWriter(PATH, true));

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
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    /**
     * 매개변수로 받은 기간 동안의 우마 리스트를 반환합니다.
     *
     * @param month 검색할 월
     * @param year  검색할 년도
     * @return {@code List<String[]>}, String[]의 [0] = 이름, [1] = 우마
     */
    public List<String[]> getUmaList(int month, int year) {
        return getUmaList(month, year, true);
    }

    /**
     * 전체 기간의 우마 리스트를 반환합니다.
     *
     * @return {@code List<String[]>}, String[]의 [0] = 이름, [1] = 우마
     */
    public List<String[]> getUmaList() {
        return getUmaList(0, 0, false);
    }

    /**
     * 일정 기간 내 {@code filter}국 이상의 유저만 집계한 우마 리스트를 반환합니다.
     *
     * @param month  검색할 월
     * @param year   검색할 년도
     * @param filter 기준 국 수
     * @return {@code List<String[]>}, String[]의 [0] = 이름, [1] = 우마
     */
    public List<String[]> getFilteredUmaList(int month, int year, int filter) {
        return getUmaList(month, year).stream().filter(
                var -> getGameCountFilteredList(month, year, filter).contains(var[0])
        ).toList();
    }

    /**
     * {@code filter}국 이상의 유저만 집계한 우마 리스트를 반환합니다.
     *
     * @param filter 기준 국 수
     * @return {@code List<String[]>}, String[]의 [0] = 이름, [1] = 우마
     */
    public List<String[]> getFilteredUmaList(int filter) {
        return getUmaList().stream().filter(
                var -> getGameCountFilteredList(filter).contains(var[0])
        ).toList();
    }


}
