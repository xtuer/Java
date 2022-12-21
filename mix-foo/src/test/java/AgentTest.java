import misc.auto.agent.AgentJob;
import misc.auto.agent.AgentRunnerHelper;
import org.junit.Test;

public class AgentTest {

    @Test
    public void testExecuteScript() {
        String scriptContent = "for ((i=0;i<20;i++));do echo $i; sleep 1; done; echo $@";
        AgentJob job = AgentRunnerHelper.executeScript("127.0.0.1", 8080, "x.sh", scriptContent, "-k1 \"v1\" -k2 \"v2\"", AgentJob.SCRIPT_TYPE_SHELL);
        System.out.println(job);
    }
}
