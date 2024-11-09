package bean;

import lombok.Data;

@Data
public class CopyBean {
    private String name;
    private String email;
    private Boolean gender;
    private InnerBean_1 inner_1;

    @Data
    public static class InnerBean_1 {
        private String innerName;
        private Boolean innerGender;
        private InnerBean_1_1 inner_1_1;

        public static class InnerBean_1_1 {

        }
    }
}
