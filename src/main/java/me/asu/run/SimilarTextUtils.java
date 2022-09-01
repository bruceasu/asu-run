package me.asu.run;

import java.io.IOException;
import java.util.*;


public class SimilarTextUtils {

    public static double getSimilarity(String pattern, String testDoc) {
        if (pattern != null && pattern.trim().length() > 0 && testDoc != null
                && testDoc.trim().length() > 0) {
            HashMap<Character, Integer[]> AlgorithmMap = new HashMap<Character, Integer[]>();
            // Encapsulate the characters and the total number of occurrences
            // in the two strings into an AlgorithmMap
            for (int i = 0; i < pattern.length(); i++) {
                char ch = pattern.charAt(i);
                Integer[] fq = null;
                try {
                    fq = AlgorithmMap.get(ch);
                } catch (Exception e) {
                } finally {
                    if (fq != null && fq.length == 2) {
                        fq[0]++;
                    } else {
                        fq    = new Integer[2];
                        fq[0] = 1;
                        fq[1] = 0;
                        AlgorithmMap.put(ch, fq);
                    }
                }
            }

            for (int i = 0; i < testDoc.length(); i++) {
                char ch = testDoc.charAt(i);
                Integer[] fq = null;
                try {
                    fq = AlgorithmMap.get(ch);
                } catch (Exception e) {
                } finally {
                    if (fq != null && fq.length == 2) {
                        fq[1]++;
                    } else {
                        fq    = new Integer[2];
                        fq[0] = 0;
                        fq[1] = 1;
                        AlgorithmMap.put(ch, fq);
                    }
                }
            }

            double sqdoc1 = 0;
            double sqdoc2 = 0;
            double denominator = 0;
            for (Map.Entry<Character, Integer[]> par : AlgorithmMap.entrySet()) {
                Integer[] c = par.getValue();
                denominator += c[0] * c[1];
                sqdoc1 += c[0] * c[0];
                sqdoc2 += c[1] * c[1];
            }
            return denominator / Math.sqrt(sqdoc1 * sqdoc2);
        } else {
            return 0;
        }
    }

    public static List<String> topSimilarString(String standardString,
            int topK,
            String... testString) throws IOException {
        // input data check
        if (isBlank(standardString) || testString == null
                || testString.length == 0) {
            return Collections.emptyList();
        }

        // adjust
        if (topK < 1) {
            topK = 1;
        }

        //System.out.println("compare " + standardString + " with: ");
        ArrayList<Map> result = new ArrayList<Map>();

        for (String test : testString) {
            //System.out.println(test);
            double x = getSimilarity(standardString, test);
            Map m = new HashMap();
            m.put("str", test);
            m.put("score", x);
            result.add(m);
            //System.out.printf("%s vs %s = %f \n", standardString, test, x);
        }
        // sort
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                double a = (Double) o1.get("score");
                double b = (Double) o2.get("score");
                double c = b - a;
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        ArrayList<String> list = new ArrayList<String>(topK);
        for (int i = 0; i < topK && i < result.size(); i++) {
            list.add((String) result.get(i).get("str"));
        }

        return list;
    }

    public static boolean isBlank(CharSequence cs) {
        if (null == cs) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {

        String str1 = "cmd";
        String[] strs = new String[]{"静默安装.cmd", "zkServer.cmd.lnk",
                "win_postinstall.cmd"};

        ArrayList<Map> result = new ArrayList<Map>();

        for (String str2 : strs) {
            double x = getSimilarity(str1, str2);
            Map m = new HashMap();
            m.put("str", str2);
            m.put("score", x);
            result.add(m);
            System.out.printf("%s vs %s = %f \n", str1, str2, x);
        }
        // sort
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                double a = (Double) o1.get("score");
                double b = (Double) o2.get("score");
                double c = b - a;
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        System.out.println(str1 + " similar with:");
        for (Map m : result) {
            System.out.println(m.get("score") + "\t" + m.get("str"));
        }

        HashSet<String> set = new HashSet<String>();
        for (String test : strs) {
            set.add(test);
        }
        List<String> strings = topSimilarString(str1, 3, set.toArray(new String[0]));
        System.out.println(strings);
    }

}
