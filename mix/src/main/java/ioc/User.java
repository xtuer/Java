package ioc;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
public class User {
    private Long id;
    private String username;
    private String email;
    private boolean isLast;
    private List<String> roles = new LinkedList<>();

    private long adminId = 10;

    public User() {
    }

    public User(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(Long id, String username, String email, String... roles) {
        this.id = id;
        this.email = email;
        this.username = username;

        for (String role : roles) {
            this.roles.add(role);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public static void main(String[] args) {
        User user = new User();
        System.out.println(JSON.toJSONString(user));
    }
}
