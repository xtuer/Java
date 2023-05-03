package xtuer.bean;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class User {
    private int userId;

    @NotBlank(message = "Username 不能为空")
    private String username;

    private String password;
}
