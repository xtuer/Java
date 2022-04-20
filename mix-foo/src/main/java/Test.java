public class Test {
    public static void main(String[] args) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append("One").append("Two");
        System.out.println(sb);
        sb = new StringBuilder(sb.substring(0, sb.length() - 1)); // 去掉最后一个 $
        System.out.println(sb);
    }
}
