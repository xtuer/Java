package util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DictionaryHandler {
    public static void main(String[] args) throws Exception {
        String pattern = "(.+?)\\s+(\\[.+])(.+)";
        String tableHeader = "| 单词 | 音标 | 解释 |\n| ---- | ---- | ---- |\n";
        StringBuilder buf = new StringBuilder("---\ntag: Words\n---\n\n");
        Files.readAllLines(Paths.get("/Users/biao/Desktop/大学英语四级大纲单词表.dict")).forEach(line -> {
            line = line.replace(".", ". ");
            line = line.trim();

            if (line.length() == 1) {
                buf.append("\n## ")
                        .append(line)
                        .append("\n\n")
                        .append(tableHeader);
                return;
            }

            int pos1 = line.indexOf("[");
            int pos2 = line.indexOf("]");

            if (pos1 != -1 && pos2 != -1) {
                String word = line.substring(0, pos1).trim();
                String pronunciation = line.substring(pos1, pos2+1);
                String meaning = line.substring(pos2+1).trim();
                String tr = String.format("| %s | %s | %s |\n", word, pronunciation, meaning);
                buf.append(tr);
            } else if (line.contains(" ")){
                int pos3 = line.indexOf(" ");
                String word = line.substring(0, pos3);
                String meaning = line.substring(pos3).trim();
                String tr = String.format("| %s | | %s |\n", word, meaning);
                buf.append(tr);
            }
        });

        System.out.println(buf);
        FileUtils.writeStringToFile(new File("/Users/biao/Desktop/table.dict"), buf.toString(), StandardCharsets.UTF_8);
    }
}
