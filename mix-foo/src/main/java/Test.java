import org.springframework.web.client.RestTemplate;

public class Test {
    public static void main(String[] args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String result = restTemplate.getForObject("http://www.baidu.com", String.class);
        System.out.println(result);
        System.out.println("end...");
    }
}
