package calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Application {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = br.readLine();

        line.split(".");
        int[] sum = new int[line.length()];
        int result =0;

        for (int i = 0; i < line.length(); i++) {
            result += sum[i];
        }
        return result;
    }
}
