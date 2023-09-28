package kuro9.mahjongbot;

import kuro9.mahjongbot.data.PaiDiscardMachiData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MachiCalculator {
    public static String getString(HashMap<Character, int[]> machi) {
        StringBuilder result = new StringBuilder();
        for (char pai : new char[]{'m', 'p', 's', 'z'}) {
            if (machi.get(pai).length != 0) {
                result.append(Arrays.stream(machi.get(pai)).mapToObj(String::valueOf).collect(Collectors.joining("")));
                result.append(pai);
            }
        }
        return result.toString();
    }

    /**
     * 타패 후 패의 대기패 해시맵을 반환합니다. 패의 개수는 3n+2 개 이어야 합니다.
     * 예시: 234만 23456통 234삭 서서 쯔모 5삭
     * <pre><code>
     *     getMachi("234m23456p2345s33z")
     * </code></pre>
     *
     * @param te_hai 손패 (m=만수, p=통수, s=삭수, z=자패)
     * @return 대기패
     */
    public static List<PaiDiscardMachiData> getTenpaiMachi(String te_hai) {
        LinkedList<PaiDiscardMachiData> result = new LinkedList<>();
        ArrayList<Integer> te_hai_int = getIntList(te_hai);
        te_hai_int.stream().distinct().forEach(
                pai -> {
                    var get_hand_list = getIntList(te_hai);
                    get_hand_list.remove(pai);
                    var machi_list = getMachiList(get_hand_list);
                    if (!machi_list.isEmpty()) {
                        var data = new PaiDiscardMachiData(pai);
                        data.machi = intListToMap(machi_list);
                        data.nokoru_pai = machi_list.size() * 4;
                        machi_list.forEach(
                                machi -> {
                                    data.nokoru_pai -= te_hai_int.stream().filter(i -> Objects.equals(i, machi)).count();
                                }
                        );
                        result.add(data);
                    }
                }
        );
//        for (var entry : result.entrySet()) {
//            if(entry.getValue().entrySet().stream().allMatch(value->value.getValue().length==0)
//        }
        return result;
    }

    /**
     * 패의 대기패를 반환합니다. 패의 개수는 3n+1 개 이어야 합니다.
     * 예시: 234만 23456통 234삭 서서
     * <pre><code>
     *     getMachi("234m23456p234s33z")
     * </code></pre>
     *
     * @param te_hai 손패 (m=만수, p=통수, s=삭수, z=자패)
     * @return 대기패
     */
    public static HashMap<Character, int[]> getMachi(String te_hai) {
        return intListToMap(getMachiList(getIntList(te_hai)));
    }

    /**
     * 패의 대기패를 반환합니다. 패의 개수는 3n+1 개 이어야 합니다.
     * 예시: 234만 23456통 234삭 서서
     * <pre><code>
     *     getMachi("234m23456p234s33z")
     * </code></pre>
     *
     * @param te_hai 손패 (m=만수, p=통수, s=삭수, z=자패)
     * @return 대기패
     */
    public static HashMap<Character, int[]> getMachi(List<Integer> te_hai) {
        return intListToMap(getMachiList(te_hai));
    }

    public static ArrayList<Integer> getIntList(String hand) {
        ArrayList<Integer> te_hai_int = new ArrayList<>();
        Pattern[] reg = new Pattern[]{
                Pattern.compile("(\\d*)m"),
                Pattern.compile("(\\d*)p"),
                Pattern.compile("(\\d*)s"),
                Pattern.compile("(\\d*)z")
        };
        for (int i = 0; i < 3; i++) {
            Matcher m = reg[i].matcher(hand);
            if (m.find()) {
                int finalI = i;
                m.group(1).chars().map(ch -> (ch - '0' + (10 * finalI))).forEach(te_hai_int::add);
            }
        }
        Matcher m = reg[3].matcher(hand);
        if (m.find()) m.group(1).chars().map(ch -> ((ch - '0' + 2) * 10 + 1)).forEach(te_hai_int::add);
        return te_hai_int;
    }

    /**
     * 계산 결과를 맵으로 변환합니다.
     *
     * @param list int로 이루어진 패 정보
     * @return 키가 m, p, s, z인 대기패 정보
     */
    private static HashMap<Character, int[]> intListToMap(List<Integer> list) {
        HashMap<Character, int[]> result = new HashMap<>();
        result.put('m', list.stream().filter(data -> data < 10).mapToInt(i -> i).toArray());
        result.put('p', list.stream().filter(data -> 10 < data && data < 20).mapToInt(i -> i - 10).toArray());
        result.put('s', list.stream().filter(data -> 20 < data && data < 30).mapToInt(i -> i - 20).toArray());
        result.put('z', list.stream().filter(data -> 30 < data).mapToInt(i -> i / 10 - 2).toArray());
        return result;
    }

    private static List<Integer> getMachiList(List<Integer> te_hai) {
        LinkedList<Integer> result = new LinkedList<>();
        LinkedList<Integer> black_list = new LinkedList<>();
        te_hai.sort(Comparator.naturalOrder());

        //연산을 하지 않아도 되는 블랙리스트 구함
        for (int i = 1; i <= 29; i++) {
            if (i % 10 == 0) black_list.add(i);
            boolean add = true;
            for (int num : te_hai) {
                if (Math.abs(num - i) <= 2) {
                    add = false;
                    break;
                }
            }
            if (add) black_list.add(i);
        }
        for (int i = 30; i <= 91; i++) {
            if (i % 10 != 1) black_list.add(i);
            else if (!te_hai.contains(i)) black_list.add(i);
        }

        int[] black_array = black_list.stream().mapToInt(i -> i).toArray();

        //대기패를 모두 대입해 몸통4 머리1 만족하는지 확인
        for (int i = 1; i <= 91; i++) {
            if (Arrays.binarySearch(black_array, i) >= 0) continue;
            LinkedList<Integer> temp_tehai = new LinkedList<>(te_hai);
            for (int j = 0; j < temp_tehai.size(); j++) {
                if (temp_tehai.get(j) >= i) {
                    temp_tehai.add(j, i);
                    break;
                }
            }
            if (temp_tehai.size() == te_hai.size()) temp_tehai.add(i);
            for (int atama : getDuplicated(temp_tehai)) {
                LinkedList<Integer> no_atama_tehai = new LinkedList<>(temp_tehai);
                no_atama_tehai.remove((Object) atama);
                no_atama_tehai.remove((Object) atama);

                if (isContainHai(te_hai, i) != 4 && isValidMentsu(no_atama_tehai, 0)) {
                    result.add(i);
                    break;
                }

            }
        }
        //치또이인지 확인
        ArrayList<Integer> chitoi_hai = getDuplicated(te_hai);
        if (chitoi_hai.size() == 6) {
            te_hai.removeAll(chitoi_hai);
            if (te_hai.size() == 1) result.add(te_hai.get(0));
        }
        return result.stream().distinct().collect(Collectors.toList());
    }

    private static ArrayList<Integer> getDuplicated(List<Integer> original) {
        ArrayList<Integer> dup_list = new ArrayList<>(6);
        for (int i = 0; i < original.size(); ) {
            int hai = original.get(i);
            int last_idx = original.lastIndexOf(hai);
            int first_idx = original.indexOf(hai);
            if (first_idx != last_idx) {
                dup_list.add(hai);
                i += (last_idx - first_idx + 1);
            }
            else i++;
        }
        return dup_list;
    }

    private static int isContainHai(List<Integer> te_hai, int hai) {
        if (!te_hai.contains(hai)) return 0;
        return te_hai.lastIndexOf(hai) - te_hai.indexOf(hai) + 1;
    }

    /**
     * 모든 패가 valid한 멘쯔인지 검사합니다.
     *
     * @param list 검사 대상 손패
     * @return 결과
     */
    private static boolean isValidMentsu(List<Integer> list, int last_anko) {
        if (list.size() % 3 != 0) return false;

        LinkedList<Integer> temp_list = new LinkedList<>(list);

        int anko_able = 0;
        for (int i = last_anko + 1; i <= 91; i++) {
            if (isContainHai(temp_list, i) >= 3) {
                anko_able = i;
                break;
            }
        }
        if (anko_able == 0) return isValidSyuntsu(temp_list);
        else {
            boolean result = isValidMentsu(temp_list, anko_able);
            for (int i = 0; i < 3; i++) temp_list.remove((Object) anko_able);
            return result || isValidMentsu(temp_list, anko_able);
        }
    }

    /**
     * 모든 패가 valid한 슌쯔인지 검사합니다.
     *
     * @param list 검사 대상 손패
     * @return 결과
     */
    private static boolean isValidSyuntsu(List<Integer> list) {
        if (list.size() % 3 != 0) return false;

        LinkedList<Integer> temp_list = new LinkedList<>(list);
        for (int i = 1; i <= 27; i++) {
            while (temp_list.contains(i) && temp_list.contains(i + 1) && temp_list.contains(i + 2)) {
                temp_list.remove((Object) i);
                temp_list.remove((Object) (i + 1));
                temp_list.remove((Object) (i + 2));
            }
            if (temp_list.isEmpty()) return true;
        }
        return false;
    }

}
