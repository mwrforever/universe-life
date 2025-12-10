package com.universe.life.common.message;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 异常消息定义接口
 * 统一管理系统中常用的错误描述信息
 *
 * @author 毛伟然
 * @since 2025/11/3 16:47
 */
public interface ExceptionMessage {

    // ==================== 通用错误消息 ====================
    String SUCCESS = "操作成功";
    String COMMON_ERROR = "系统异常，请稍后重试";
    String PARAM_ERROR = "参数错误";
    String DATA_NOT_FOUND = "数据不存在";
    String DATA_ALREADY_EXISTS = "数据已存在";
    String OPERATION_NOT_ALLOWED = "操作不被允许";
    String REQUEST_TIMEOUT = "请求超时";
    String SERVICE_UNAVAILABLE = "服务暂不可用";

    // ==================== 认证授权错误消息 ====================
    String UNAUTHORIZED = "用户未授权";
    String FORBIDDEN = "用户无权限访问";
    String AUTH_FAILED = "认证失败";
    String AUTH_TOKEN_MISSING = "缺少认证令牌";
    String AUTH_TOKEN_INVALID = "认证令牌无效";
    String AUTH_TOKEN_EXPIRED = "认证令牌已过期";
    String ACCESS_DENIED = "权限不足";
    String USER_NOT_FOUND = "用户不存在";
    String PASSWORD_INCORRECT = "密码错误";
    String ACCOUNT_LOCKED = "账户已被锁定";
    String ACCOUNT_DISABLED = "账户已被禁用";
    String ACCOUNT_EXPIRED = "账户已过期";
    String LOGIN_REQUIRED = "请先登录";
    String SESSION_TIMEOUT = "会话已超时，请重新登录";
    String CAPTCHA_ERROR = "验证码错误";
    String TOO_MANY_ATTEMPTS = "登录尝试次数过多，请稍后再试";
    String USER_ALREADY_EXISTS = "用户已存在";
    String CLIENT_NOT_FOUND = "客户端不存在";
    String USER_INFO_NOT_FOUND = "用户认证信息不存在";
    String TOKEN_GENERATION_FAILED = "令牌生成失败";
    String CAPTCHA_ALREADY_EXPIRED = "验证码已过期";
    String AUTHORIZATION_CODE_REQUIRED = "请提供授权码";
    String AUTHORIZATION_CODE_EXPIRED = "授权码已过期，请重新获取";
    String AUTHORIZATION_CODE_INVALID = "授权码无效";
    String CAPTCHA_ALREADY_EXISTS = "验证码已存在，请勿重复提交";
    String PHONE_EMAIL_FORMAT_ERROR = "手机号或邮箱格式错误";
    String REQUEST_METHOD_NOT_ALLOWED = "请求方法 {0} 不允许";

    // ==================== 业务错误消息 ====================
    String BUSINESS_ERROR = "业务处理失败";
    String PARAM_REQUIRED = "参数 {0} 不能为空";
    String PARAM_INVALID = "参数 {0} 格式不正确";
    String PARAM_OUT_OF_RANGE = "参数 {0} 超出有效范围";
    String RECORD_NOT_FOUND = "记录 {0} 不存在";
    String RECORD_ALREADY_EXISTS = "记录 {0} 已存在";
    String OPERATION_FAILED = "操作 {0} 失败";
    String RESOURCE_BUSY = "资源繁忙，请稍后重试";
    String QUOTA_EXCEEDED = "超出配额限制";
    String STATUS_INVALID = "状态不正确，无法执行此操作";

    // ==================== 数据库错误消息 ====================
    String DATABASE_ERROR = "数据库操作失败";
    String DB_CONNECTION_ERROR = "数据库连接失败";
    String DB_QUERY_ERROR = "数据查询失败";
    String DB_UPDATE_ERROR = "数据更新失败";
    String DB_TRANSACTION_ERROR = "事务执行失败";
    String DB_CONSTRAINT_VIOLATION = "数据约束违反";
    String DB_DEADLOCK = "数据库死锁，请重试";
    String DB_TIMEOUT = "数据库操作超时";
    String DB_CONNECTION_POOL_EXHAUSTED = "数据库连接池已耗尽";

    // ==================== 网络错误消息 ====================
    String NETWORK_ERROR = "网络连接异常";
    String CONNECTION_TIMEOUT = "连接超时";
    String CONNECTION_REFUSED = "连接被拒绝";
    String HOST_NOT_FOUND = "主机无法访问";
    String NETWORK_UNREACHABLE = "网络不可达";
    String REMOTE_SERVICE_ERROR = "远程服务异常";
    String API_CALL_FAILED = "API调用失败";
    String DATA_TRANSFER_ERROR = "数据传输失败";

    // ==================== 系统错误消息 ====================
    String SYSTEM_ERROR = "系统内部错误";
    String CONFIG_ERROR = "配置错误";
    String FILE_NOT_FOUND = "文件不存在";
    String FILE_READ_ERROR = "文件读取失败";
    String FILE_WRITE_ERROR = "文件写入失败";
    String FILE_PERMISSION_DENIED = "文件权限不足";
    String DISK_SPACE_FULL = "磁盘空间不足";
    String MEMORY_INSUFFICIENT = "内存不足";
    String THREAD_INTERRUPTED = "线程被中断";
    String CLASS_NOT_FOUND = "类加载失败";
    String METHOD_NOT_FOUND = "方法不存在";

    // ==================== 第三方服务错误消息 ====================
    String THIRD_PARTY_ERROR = "第三方服务异常";
    String PAYMENT_ERROR = "支付服务异常";
    String SMS_ERROR = "短信发送失败";
    String EMAIL_ERROR = "邮件发送失败";
    String WECHAT_ERROR = "微信服务异常";
    String ALIYUN_ERROR = "阿里云服务异常";
    String TENCENT_ERROR = "腾讯云服务异常";
    String DEVICE_MISMATCH = "设备不匹配，请重新登录";
    String USER_NOT_EXIST = "账户不存在";
    String PARENT_RESOURCE_NOT_FOUND = "父资源不能为空";
    String PARENT_RESOURCE_NOT_BE_SELF = "父资源不能为自身";
    String ROLE_NOT_FOUND = "角色不存在";
    String RESOURCE_NOT_FOUND = "部分资源不存在";
    String PART_OF_ROLE_NOT_FOUND = "部分角色不存在";

    /**
     * 消息格式化工具类
     */
    class Formatter {

        /**
         * 格式化消息，支持参数替换
         *
         * @param template 消息模板
         * @param params   参数数组
         * @return 格式化后的消息
         */
        public static String format(String template, Object... params) {
            if (template == null) {
                return "";
            }
            if (params == null || params.length == 0) {
                return template;
            }
            return MessageFormat.format(template, params);
        }

        /**
         * 获取参数必填错误消息
         *
         * @param paramName 参数名称
         * @return 错误消息
         */
        public static String paramRequired(String paramName) {
            return format(PARAM_REQUIRED, paramName);
        }

        /**
         * 获取参数无效错误消息
         *
         * @param paramName 参数名称
         * @return 错误消息
         */
        public static String paramInvalid(String paramName) {
            return format(PARAM_INVALID, paramName);
        }

        /**
         * 获取记录不存在错误消息
         *
         * @param recordName 记录名称
         * @return 错误消息
         */
        public static String recordNotFound(String recordName) {
            return format(RECORD_NOT_FOUND, recordName);
        }

        /**
         * 获取记录已存在错误消息
         *
         * @param recordName 记录名称
         * @return 错误消息
         */
        public static String recordAlreadyExists(String recordName) {
            return format(RECORD_ALREADY_EXISTS, recordName);
        }

        /**
         * 获取操作失败错误消息
         *
         * @param operationName 操作名称
         * @return 错误消息
         */
        public static String operationFailed(String operationName) {
            return format(OPERATION_FAILED, operationName);
        }

        /**
         * 获取参数超出范围错误消息
         *
         * @param paramName 参数名称
         * @return 错误消息
         */
        public static String paramOutOfRange(String paramName) {
            return format(PARAM_OUT_OF_RANGE, paramName);
        }
    }

    /**
     * 错误码消息映射工具类
     */
    class CodeMapping {

        /**
         * 错误码到消息的映射
         */
        private static final Map<Integer, String> ERROR_MESSAGE_MAP = new HashMap<>();

        static {
            // 通用错误
            ERROR_MESSAGE_MAP.put(200, SUCCESS);
            ERROR_MESSAGE_MAP.put(400, PARAM_ERROR);
            ERROR_MESSAGE_MAP.put(401, UNAUTHORIZED);
            ERROR_MESSAGE_MAP.put(403, FORBIDDEN);
            ERROR_MESSAGE_MAP.put(404, DATA_NOT_FOUND);
            ERROR_MESSAGE_MAP.put(405, OPERATION_NOT_ALLOWED);
            ERROR_MESSAGE_MAP.put(408, REQUEST_TIMEOUT);
            ERROR_MESSAGE_MAP.put(500, SYSTEM_ERROR);
            ERROR_MESSAGE_MAP.put(503, SERVICE_UNAVAILABLE);

            // 业务错误 (2000-2999)
            ERROR_MESSAGE_MAP.put(2000, BUSINESS_ERROR);
            ERROR_MESSAGE_MAP.put(2001, PARAM_ERROR);
            ERROR_MESSAGE_MAP.put(2002, DATA_NOT_FOUND);
            ERROR_MESSAGE_MAP.put(2003, DATA_ALREADY_EXISTS);
            ERROR_MESSAGE_MAP.put(2004, OPERATION_NOT_ALLOWED);

            // 数据库错误 (3000-3999)
            ERROR_MESSAGE_MAP.put(3000, DATABASE_ERROR);
            ERROR_MESSAGE_MAP.put(3001, DB_CONNECTION_ERROR);
            ERROR_MESSAGE_MAP.put(3002, DB_QUERY_ERROR);
            ERROR_MESSAGE_MAP.put(3003, DB_UPDATE_ERROR);
            ERROR_MESSAGE_MAP.put(3004, DB_TRANSACTION_ERROR);
            ERROR_MESSAGE_MAP.put(3005, DB_CONSTRAINT_VIOLATION);

            // 网络错误 (4000-4999)
            ERROR_MESSAGE_MAP.put(4000, NETWORK_ERROR);
            ERROR_MESSAGE_MAP.put(4001, CONNECTION_TIMEOUT);
            ERROR_MESSAGE_MAP.put(4002, CONNECTION_REFUSED);
            ERROR_MESSAGE_MAP.put(4003, SERVICE_UNAVAILABLE);

            // 认证授权错误 (5000-5999)
            ERROR_MESSAGE_MAP.put(5000, AUTH_FAILED);
            ERROR_MESSAGE_MAP.put(5001, ACCESS_DENIED);
            ERROR_MESSAGE_MAP.put(5002, AUTH_TOKEN_EXPIRED);
            ERROR_MESSAGE_MAP.put(5003, AUTH_TOKEN_INVALID);
            ERROR_MESSAGE_MAP.put(5004, AUTH_TOKEN_MISSING);
            ERROR_MESSAGE_MAP.put(5005, USER_NOT_FOUND);
            ERROR_MESSAGE_MAP.put(5006, PASSWORD_INCORRECT);
            ERROR_MESSAGE_MAP.put(5007, ACCOUNT_LOCKED);
            ERROR_MESSAGE_MAP.put(5008, ACCOUNT_DISABLED);

            // 系统错误 (6000-6999)
            ERROR_MESSAGE_MAP.put(6000, SYSTEM_ERROR);
            ERROR_MESSAGE_MAP.put(6001, CONFIG_ERROR);
            ERROR_MESSAGE_MAP.put(6002, FILE_NOT_FOUND);
            ERROR_MESSAGE_MAP.put(6003, FILE_READ_ERROR);
            ERROR_MESSAGE_MAP.put(6004, FILE_WRITE_ERROR);
            ERROR_MESSAGE_MAP.put(6005, MEMORY_INSUFFICIENT);

            // 第三方服务错误 (7000-7999)
            ERROR_MESSAGE_MAP.put(7000, THIRD_PARTY_ERROR);
            ERROR_MESSAGE_MAP.put(7001, PAYMENT_ERROR);
            ERROR_MESSAGE_MAP.put(7002, SMS_ERROR);
            ERROR_MESSAGE_MAP.put(7003, EMAIL_ERROR);
            ERROR_MESSAGE_MAP.put(7004, WECHAT_ERROR);
        }

        /**
         * 根据错误码获取默认消息
         *
         * @param errorCode 错误码
         * @return 错误消息
         */
        public static String getByCode(int errorCode) {
            return ERROR_MESSAGE_MAP.getOrDefault(errorCode, COMMON_ERROR);
        }

        /**
         * 根据错误码获取消息，支持自定义消息
         *
         * @param errorCode     错误码
         * @param customMessage 自定义消息
         * @return 错误消息
         */
        public static String getByCodeOrCustom(int errorCode, String customMessage) {
            return customMessage != null && !customMessage.trim().isEmpty()
                    ? customMessage
                    : getByCode(errorCode);
        }

        /**
         * 获取所有错误消息映射（用于调试和文档）
         *
         * @return 错误消息映射的副本
         */
        public static Map<Integer, String> getAllErrorMessages() {
            return new HashMap<>(ERROR_MESSAGE_MAP);
        }
    }
}