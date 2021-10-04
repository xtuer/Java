package bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Result<T> {
    private T data;

    public Result() {}

    public Result(T data) {
        this.data = data;
    }
}
