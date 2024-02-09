package open.demo.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean {
    private Object data;
    private Integer code;
    private String msg;

    public static ResponseBean success(Object data, String msg) {
        return new ResponseBean(data, 200, msg);
    }
}
