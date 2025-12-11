package com.universe.life.auth.common.exception;

/**
 * 统一错误码常量
 *
 * @author universe-life
 */
public interface ErrorCode {

    // 通用错误码 1000-1999
    int SUCCESS = 200;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int METHOD_NOT_ALLOWED = 405;
    int INTERNAL_SERVER_ERROR = 500;
    int SERVICE_UNAVAILABLE = 503;

    // 业务错误码 2000-2999
    int BUSINESS_ERROR = 2000;
    int PARAM_ERROR = 2001;
    int DATA_NOT_FOUND = 2002;
    int DATA_ALREADY_EXISTS = 2003;
    int OPERATION_NOT_ALLOWED = 2004;

    // 数据库错误码 3000-3999
    int DATABASE_ERROR = 3000;
    int DATABASE_CONNECTION_ERROR = 3001;
    int DATABASE_QUERY_ERROR = 3002;
    int DATABASE_UPDATE_ERROR = 3003;
    int DATABASE_TRANSACTION_ERROR = 3004;
    int DATABASE_CONSTRAINT_VIOLATION = 3005;

    // 网络错误码 4000-4999
    int NETWORK_ERROR = 4000;
    int CONNECTION_TIMEOUT = 4001;
    int CONNECTION_REFUSED = 4002;
    int SERVICE_UNAVAILABLE_ERROR = 4003;


    // 系统错误码 6000-6999
    int SYSTEM_ERROR = 6000;
    int CONFIG_ERROR = 6001;
    int FILE_NOT_FOUND = 6002;
    int FILE_READ_ERROR = 6003;
    int FILE_WRITE_ERROR = 6004;
    int MEMORY_ERROR = 6005;

    // 第三方服务错误码 7000-7999
    int THIRD_PARTY_ERROR = 7000;
    int PAYMENT_ERROR = 7001;
    int SMS_ERROR = 7002;
    int EMAIL_ERROR = 7003;
    int WECHAT_ERROR = 7004;
}