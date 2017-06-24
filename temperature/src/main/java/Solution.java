import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt(); // the number of temperatures to analyse
        in.nextLine();
        String tempsAsString = in.nextLine(); // the n temperatures expressed as integers ranging from -273 to 5526

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        List<String> temps = Arrays.asList(tempsAsString.split(" "));
        System.err.println(temps);

        Optional<String> result = temps.stream()
                .filter(tempAsString -> tempAsString.trim().length() > 0)
                .min((s1, s2) -> {
                    Double d1 = Double.parseDouble(s1);
                    Double d2 = Double.parseDouble(s2);

                    double absD1 = Math.abs(d1);
                    double absD2 = Math.abs(d2);

                    if (absD1 == absD2) {
                        return d1 >= 0 ? -1 : 1;
                    } else {
                        return Double.valueOf(absD1).compareTo(Double.valueOf(absD2));
                    }
                });

        System.out.println(result.orElse("0"));
    }
}