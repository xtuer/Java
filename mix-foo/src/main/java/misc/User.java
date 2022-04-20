package misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;
import static java.util.Comparator.comparing;
import static java.util.Collections.reverseOrder;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class User {
    private int teamId;
    private String username;

    public User(int teamId, String username) {
        this.teamId = teamId;
        this.username = username;
    }

    public static List<User> prepareData() {
        List<User> users = new LinkedList<>();
        users.add(new User(1, "Alice"));
        users.add(new User(1, "Bob"));
        users.add(new User(1, "John"));
        users.add(new User(2, "Bob"));
        users.add(new User(2, "Steven"));
        users.add(new User(3, "John"));
        users.add(new User(3, "Loa"));

        return users;
    }

    public static void main(String[] args) {
        // 统计出现频率
        List<Integer> ns = Arrays.asList(1, 2, 3, 2, 4, 3, 2);

        // 频率为 Long
        Map<Integer, Long> map1 = ns.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        System.out.println(map1);
    }
}
