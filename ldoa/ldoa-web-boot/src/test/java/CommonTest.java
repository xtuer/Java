import com.xtuer.bean.Role;
import com.xtuer.bean.audit.AuditStep;
import com.xtuer.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CommonTest {
    @Test
    public void testLambdaMax1() {
        List<AuditStep> items = prepareDate();
        int maxStep = items.stream().map(AuditStep::getStep).max(Comparator.comparing(Integer::valueOf)).orElse(0);
        Assert.isTrue(maxStep == 5, "");
    }

    @Test
    public void testLambdaMax2() {
        List<AuditStep> items = prepareDate();
        int maxStep = items.stream().mapToInt(AuditStep::getStep).max().orElse(0);
        Assert.isTrue(maxStep == 5, "");
    }

    private List<AuditStep> prepareDate() {
        List<AuditStep> items = new LinkedList<>();
        items.add(new AuditStep().setStep(1));
        items.add(new AuditStep().setStep(2));
        items.add(new AuditStep().setStep(3));
        items.add(new AuditStep().setStep(4));
        items.add(new AuditStep().setStep(5));

        return items;
    }

    /**
     * 一天的开始和结束时间测试
     */
    @Test
    public void testDate() {
        System.out.println(Utils.dayStart(new Date()));
        System.out.println(Utils.dayEnd(new Date()));
    }

    @Test
    public void toJson() {
        System.out.println(Utils.toJson(Role.ROLE_ADMIN)); // ROLE_ADMIN
    }
}
