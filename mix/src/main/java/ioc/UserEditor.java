package ioc;

import java.beans.PropertyEditorSupport;

/**
 * 自定义字符串转换为对象
 */
public class UserEditor extends PropertyEditorSupport {
    // "123, Biao, biao.mac@icloud.com"
    @Override
    public void setAsText(String text) {
        String[] components = text.split(",");

        User user = new User();
        user.setId(Long.parseLong(components[0].trim()));
        user.setUsername(components[1].trim());
        user.setEmail(components[2].trim());

        super.setValue(user);
    }

    public static void main(String[] args) {
        UserEditor editor = new UserEditor();
        editor.setAsText("123, Biao, biao.mac@icloud.com");
        System.out.println(editor.getValue());
    }
}
