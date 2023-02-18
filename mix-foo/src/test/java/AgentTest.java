import misc.auto.ndtagent.AgentConfig;
import misc.auto.ndtagent.AgentJob;
import misc.auto.ndtagent.AgentRunner;
import misc.auto.ndtagent.AgentRunnerHelper;
import org.junit.Test;

public class AgentTest {

    @Test
    public void testExecuteScript() {
        String scriptContent = "for ((i=0;i<10;i++));do echo $i; sleep 1; done; echo $@";
        AgentJob job = AgentRunnerHelper.executeScript("127.0.0.1", 12301, "x.sh", scriptContent, "-k1 \"v1\" -k2 \"v2\"", AgentJob.SCRIPT_TYPE_SHELL, true);
        System.out.println(job);
    }

    @Test
    public void testExecuteScript2() {
        AgentConfig config = new AgentConfig();
        config.setAgentPort(12301);

        AgentRunner runner = new AgentRunner(config);
        AgentJob job = runner.executeScript("127.0.0.1", "x.sh", "-k1 \"v1\" -k2 \"v2\"");
        System.out.println(job.getState());
        System.out.println(job.isSuccess());
    }

    @Test
    public void testExecuteScriptDirectly() {
        AgentConfig config = new AgentConfig();
        config.setAgentPort(12301);

        AgentRunner runner = new AgentRunner(config);
        AgentJob job = runner.executeShellScriptDirectly("127.0.0.1", "x.sh", "for ((i=0;i<6;i++));do echo $i; sleep 1; done; echo $@", "");
        System.out.println(job);
    }
}
