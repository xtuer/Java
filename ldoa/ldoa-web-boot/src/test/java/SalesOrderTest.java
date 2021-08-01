import com.xtuer.Application;
import com.xtuer.bean.User;
import com.xtuer.bean.sales.CustomerFinance;
import com.xtuer.bean.sales.SalesOrder;
import com.xtuer.mapper.SalesOrderMapper;
import com.xtuer.service.SalesOrderService;
import com.xtuer.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest(classes = { Application.class })
public class SalesOrderTest {
    @Autowired
    private SalesOrderService orderService;

    @Autowired
    private SalesOrderMapper oderMapper;

    @Test
    public void createOrder() {
        SalesOrder order = new SalesOrder();
        order.setProduceOrderId(1);
        order.setTopic("测试主题");
        order.setAgreementDate(new Date());
        order.setDeliveryDate(new Date());
        order.setOwnerId(1);
        order.setCustomerId(1);
        order.setCustomerContact("测试联系人");
        order.setBusiness("测试行业");
        order.setWorkUnit("测试执行单位");
        order.setRemark("测试备注");

        orderService.upsertSalesOrder(order, 0, new User());
    }

    @Test
    public void findFinance() {
        CustomerFinance finance = oderMapper.findFinanceByCustomerId(579003341602820L);
        Utils.dump(finance);
    }
}
