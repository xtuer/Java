package com.edu.training.security;

import com.edu.training.bean.User;
import com.edu.training.service.OrganizationService;
import com.edu.training.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService orgService;

    /**
     * 使用 username 加载用户的信息，如密码，权限等
     *
     * @param username 登陆表单中用户输入的用户名
     * @return 返回查找到的用户对象
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long orgId = orgService.getCurrentOrganizationId(); // 当前机构 ID
        User user  = userService.findUser(username, orgId); // 数据库中查找用户

        if (user == null) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        user = user.cloneForSecurity(); // 构建 Spring Security 需要的用户

        return user;
    }
}
