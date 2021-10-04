package bootimport;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 使用场景: 例如 Spring Boot 的 EnableAutoConfiguration 批量创建对象到 Spring 容器。
 */
public class CustomSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{"bootimport.Tom"};
        // return new String[]{Tom.class.getName()};
    }
}
