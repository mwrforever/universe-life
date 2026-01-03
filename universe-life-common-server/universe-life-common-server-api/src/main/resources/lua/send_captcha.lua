-- send_captcha.lua
-- 发送验证码的 Redis 原子操作脚本
-- 参数列表：
-- KEYS[1]: defendKey (防刷 key)
-- KEYS[2]: captchaKey (验证码 key)
-- ARGV[1]: captcha (验证码值)
-- ARGV[2]: defendExpire (防刷过期时间，秒)
-- ARGV[3]: captchaExpire (验证码过期时间，秒)
--
-- 返回值：
-- 1: 成功
-- 2: 一分钟内重复请求验证码错误
-- 3: 其它错误

-- 检查防刷 key 是否存在
local defendExists = redis.call('EXISTS', KEYS[1])

-- 如果防刷 key 存在，说明一分钟内重复请求
if defendExists == 1 then
    return 2
end

-- 设置防刷 key，过期时间为 defendExpire 秒
local defendSetResult = redis.call('SETEX', KEYS[1], ARGV[2], '1')

-- 检查防刷 key 是否设置成功
if not defendSetResult then
    return 3
end

-- 设置验证码 key，过期时间为 captchaExpire 秒
local captchaSetResult = redis.call('SETEX', KEYS[2], ARGV[3], ARGV[1])

-- 检查验证码 key 是否设置成功
if not captchaSetResult then
    return 3
end

-- 所有操作成功
return 1
