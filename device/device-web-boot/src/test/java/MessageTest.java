import com.xtuer.util.Utils;
import com.xtuer.ws.msg.ErrorMessage;
import com.xtuer.ws.msg.StatusUpMessage;
import com.xtuer.ws.msg.Message;
import org.junit.jupiter.api.Test;

public class MessageTest {
    @Test
    public void messageToJson() {
        Message msg = new ErrorMessage().setError("Exception encountered");
        System.out.println(msg.toJson());

        System.out.println(new StatusUpMessage().toJson());
    }

    @Test
    public void jsonToMessage() {
        Message msg = Utils.fromJson("{\"type\": \"ECHO\", \"content\": \"hello\"}", Message.class);
        System.out.println(msg.toJson());

        msg = Utils.fromJson("{\"gatewayId\":\"\",\"deviceId\":\"\",\"type\":\"HEARTBEAT_UP\",\"chanType\":\"\",\"address\":\"20\",\"voltage\":0.0,\"deviceType\":0,\"status\":\"\"}", StatusUpMessage.class);
        System.out.println(msg.toJson());
    }
}
