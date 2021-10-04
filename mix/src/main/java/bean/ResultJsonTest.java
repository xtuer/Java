package bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultJsonTest {
    public static void main(String[] args) throws Exception {
        User alice = new User().setUsername("Alice");
        Result<User> result = new Result<>(alice);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(result);
        System.out.println(json);

        // 泛型反序列化有 2 种方式
        // [1] Generic Using TypeReference
        Result<User> user1 = mapper.readValue(json, new TypeReference<Result<User>>() {});
        System.out.println(user1);

        // [2] Generic Using JavaType
        JavaType jt = mapper.getTypeFactory().constructParametricType(Result.class, User.class);
        Result<User> user2 = mapper.readValue(json, jt);
        System.out.println(user2);
    }
}
