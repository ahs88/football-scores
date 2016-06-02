package barqsoft.footballscores.network;

import android.support.annotation.IntDef;

/**
 * Created by shetty on 30/01/16.
 */
public class ApiError {

    private String error;
    private int errorCode;

    @IntDef({BAD_REQUEST, RESTRICTED_RESOURCES, RESOURCE_NOT_FOUND,EXCEEDED_REQUEST_LIMIT,NO_INTERNET})
    public @interface ERROR_CODES {}
    public static final int BAD_REQUEST = 400;
    public static final int RESTRICTED_RESOURCES = 403;
    public static final int RESOURCE_NOT_FOUND = 404;
    public static final int EXCEEDED_REQUEST_LIMIT = 429;
    public static final int NO_INTERNET = -1;
    public static final int ERROR_UNKNOWN = -2;




    public ApiError() {
    }

    //@ERROR_CODES
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int error_code) {
        this.errorCode = error_code;
    }


    public String getMessage() {
        return error;
    }

    public void setMessage(String error) {
        this.error = error;
    }
}
