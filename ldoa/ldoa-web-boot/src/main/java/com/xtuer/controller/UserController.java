package com.xtuer.controller;

import com.xtuer.bean.*;
import com.xtuer.mapper.UserMapper;
import com.xtuer.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作用户信息的控制器
 *
 * 注意: 由于会涉及到缓存，此类中不要直接使用 UserMapper 访问用户数据，而应该使用 UserService
 */
@RestController
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 使用用户 ID 查询用户信息
     *
     * 网址: http://localhost:8080/api/users/{userId}
     * 参数: 无
     *
     * @param userId 用户的 ID
     * @return 查询到时 payload 为用户对象, success 为 true，查询不到时 success 为 false, payload 为 null
     */
    @GetMapping(Urls.API_USERS_BY_ID)
    public Result<User> findUserById(@PathVariable long userId) {
        User user = userService.findUser(userId);
        return Result.single(user, "ID 为 " + userId + "的用户不存在");
    }

    /**
     * 查询用户，nickname 不为空时使用 LIKE 匹配，为空时返回所有用户
     *
     * 网址: http://localhost:8080/api/users
     * 参数:
     *      nickname   [可选]: 姓名 (使用 LIKE 匹配)
     *      pageNumber [可选]: 页码
     *      pageSize   [可选]: 页码
     *
     * @param nickname 姓名
     * @param page     分页
     * @return payload 为用户数组
     */
    @GetMapping(Urls.API_USERS)
    public Result<List<User>> findUsers(@RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String username,
                                        Page page) {
        nickname = StringUtils.trim(nickname);
        return Result.ok(userMapper.findUsersLikeNickname(nickname, username, page));
    }

    /**
     * 创建用户
     *
     * 网址: http://localhost:8080/api/users
     * 参数: 无
     * 请求体: 用户的 JSON 字符串
     *      username (必要): 账号
     *      nickname (必要): 姓名
     *      password (必要): 密码
     *      roles    [可选]: 角色数组
     *
     * @param user 用户
     * @return payload 为用户信息
     */
    @PostMapping(Urls.API_USERS)
    public Result<User> createUser(@RequestBody User user) {
        // 设置用户的机构 ID，然后再创建
        long orgId = super.getCurrentOrganizationId();
        user.setOrgId(orgId);
        return userService.createUser(user);
    }

    /**
     * 更新用户的昵称、头像、手机、性别、密码。注意，一次只能更新一个属性
     *
     * 网址: http://localhost:8080/api/users/{userId}
     * 参数:
     *      nickname [可选]: 昵称
     *      avatar   [可选]: 头像
     *      mobile   [可选]: 手机
     *      gender   [可选]: 性别 (0, 1, 2)
     *      oldPassword   [可选]: 旧密码
     *      newPassword   [可选]: 新密码
     *      renewPassword [可选]: 确认的密码
     *      role          [可选]: 角色
     *
     * @param userId        用户 ID
     * @param nickname      昵称
     * @param avatar        头像
     * @param mobile        手机
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     * @param renewPassword 确认的密码
     * @param gender        性别
     * @param role          角色
     * @return 1. 更新头像成功时，payload 为头像的正式 URL
     *         2. 更新其他属性成功时 payload 为空，message 为对应属性更新成功提示
     */
    @PatchMapping(Urls.API_USERS_BY_ID)
    public Result<String> patchUser(@PathVariable long userId,
                                    @RequestParam(required = false) String nickname,
                                    @RequestParam(required = false) String avatar,
                                    @RequestParam(required = false) String mobile,
                                    @RequestParam(required = false) String oldPassword,
                                    @RequestParam(required = false) String newPassword,
                                    @RequestParam(required = false) String renewPassword,
                                    @RequestParam(required = false, defaultValue = "-1") int gender,
                                    @RequestParam(required = false) Role role) {
        // 更新昵称
        if (StringUtils.isNotBlank(nickname)) {
            userService.updateUserNickname(userId, nickname.trim());
            return Result.ok(null, "昵称更新成功");
        }

        // 更新头像
        if (StringUtils.isNotBlank(avatar)) {
            avatar = userService.updateUserAvatar(userId, avatar.trim());
            return Result.single(avatar);
        }

        // 更新性别
        if (gender != -1) {
            userService.updateUserGender(userId, gender);
            return Result.ok(null, "性别更新成功");
        }

        // 更新手机
        if (StringUtils.isNotBlank(mobile)) {
            return userService.updateUserMobile(userId, mobile.trim());
        }

        // 更新密码
        if (StringUtils.isNotBlank(oldPassword) || StringUtils.isNotBlank(newPassword) || StringUtils.isNotBlank(renewPassword)) {
            return userService.updateUserPassword(userId, oldPassword, newPassword, renewPassword);
        }

        // 更新角色
        if (role != null) {
            userService.changeUserRole(userId, role);
            return Result.ok(null, "权限更新成功");
        }

        // 什么都没有更新，则认为更新失败
        // 提示: 例如前端更新昵称，输入空的昵称，服务器就什么都没有更新，返回成功，前端就认为更新成功了，页面上就会使用空的昵称
        return Result.fail("用户信息修改失败");
    }

    /**
     * 重置用户的密码
     * 网址: http://localhost:8080/api/users/{userId}/passwords/reset
     * 参数: 无
     *
     * @param userId 用户的 ID
     */
    @PutMapping(Urls.API_USER_PASSWORDS_RESET)
    public Result<String> resetUserPassword(@PathVariable long userId) {
        userService.resetUserPassword(userId);
        return Result.ok();
    }

    /**
     * 删除用户
     *
     * 网址: http://localhost:8080/api/users/{userId}
     * 参数: 无
     *
     * @param userId 用户 ID
     */
    @DeleteMapping(Urls.API_USERS_BY_ID)
    public Result<Boolean> deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        return Result.ok();
    }
}
