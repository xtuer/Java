package dsc.desensitivate;

/**
 * 敏感数据规则
 */
public interface SensitiveRule {
    // 敏感规则类型: 1. 姓名; 2. 身份证号码; 3. 手机号码; 4. 邮箱; 5. 地址; 6. 金额
    int TYPE_NAME       = 1;
    int TYPE_ID_CARD_NO = 2;
    int TYPE_MOBILE     = 3;
    int TYPE_EMAIL      = 4;
    int TYPE_ADDRESS    = 5;
    int TYPE_AMOUNT     = 6;

    /**
     * 测试传入的数据是否匹配当前敏感规则
     *
     * @param text 感觉规则测试的数据
     * @return 匹配当前规则返回 true，否则返回 false
     */
    boolean test(String text);
}
