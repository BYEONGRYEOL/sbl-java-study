package Decorator;

import java.io.*;
import java.net.URL;

public class decorator {
    public static void main(String[] args) throws IOException {

        char[] fileContent = new char[100];

        BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\sbl\\IdeaProjects\\javaStudy\\src\\main\\java\\Decorator\\test.txt")));

        br.read(fileContent);

        System.out.println(String.valueOf(fileContent));

        br.lines()
                .forEach(System.out::println); // 남은 모든 줄들 출력
        br.close();



        FileReader fr = new FileReader(new File("C:\\Users\\sbl\\IdeaProjects\\javaStudy\\src\\main\\java\\Decorator\\test.txt"));

        fileContent = new char[100];
        fr.read(fileContent);

        System.out.println(String.valueOf(fileContent));

    }
}
