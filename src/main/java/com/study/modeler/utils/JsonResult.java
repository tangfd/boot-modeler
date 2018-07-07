package com.study.modeler.utils;

/**
 * 统一JSON返回结果
 */
public class JsonResult {

    // 状态（true, false）
    private Boolean status = false;
    // 错误代码
    private String errCode;
    // 错误信息
    private String message;
    // 结果对象
    private Object result;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setResult(boolean status, Object result) {
        this.status = status;
        this.result = result;
    }

    public void setMessage(boolean status, String message) {
        this.message = message;
        this.status = status;
    }
}
