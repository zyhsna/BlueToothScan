package edu.zyh.finalproject.data;

/**
 * @author zyhsna
 */
public class JSONData {

    /**
     * <p>stateCode:状态码</p>
     * <p>data:数据</p>
     * <p>message:返回给前端消息</p>
     */
    private int stateCode;
    private Object data;
    private String message;

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "JSONData{" +
                "stateCode=" + stateCode +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
