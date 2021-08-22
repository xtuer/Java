import com.xtuer.Application;
import com.xtuer.bean.User;
import com.xtuer.mapper.UserMapper;
import com.xtuer.service.UserService;
import com.xtuer.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest(classes = { Application.class })
// @ActiveProfiles({ "mac" }) // 指定测试的 active profile (dev, default)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void findUser() {
        User user = userService.findUser("admin", 1);
        Utils.dump(user);
    }

    @Test
    public void findUserIdByRole() {
        List<Long> ids = userMapper.findUserIdsByRoles(Arrays.asList("ROLE_ADMIN_SYSTEM", "ROLE_FINANCE"));
        Utils.dump(ids);
    }
}
