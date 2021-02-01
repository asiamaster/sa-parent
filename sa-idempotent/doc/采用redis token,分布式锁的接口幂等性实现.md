# 前言

在互联网API接口中，由于网络超时、手动刷新等经常导致客户端重复提交数据到服务端，这就要求在设计API接口时做好幂等控制。尤其是在面向微服务架构的系统中，系统间的调用非常频繁，如果不做好幂等性设置，轻则会导致脏数据入库，重则导致资损。

本方案基于Redis实现一个幂等控制框架。主要思路是在调用接口时传入全局唯一的token字段，标识一个请求是否是重复请求。

# 总体思路
1）在调用接口之前先调用获取token的接口生成对应的令牌(token)，并存放在redis当中。
2）在调用接口的时候，将第一步得到的token放入请求头中。
3）解析请求头，如果能获取到该令牌，就放行，执行既定的业务逻辑，并从redis中删除该token。
4）如果获取不到该令牌，就返回错误信息(例如：请勿重复提交)

# 使用指南
## 1 application.properties配置
```
idempotent.enable=true
redis.enable=true

spring.redis.database=0
spring.redis.host=127.0.0.1
# Redis服务器连接密码（默认为空）
spring.redis.password=123456789
spring.redis.port=6379
# 连接池最大连接数（使用负值表示没有限制）
spring.redis.jedis.pool.max-active=8
# 连接池最大阻塞等待时间（使用负值表示没有限制）
#spring.redis.jedis.pool.max-wait=-1
# 连接池中的最大空闲连接
spring.redis.jedis.pool.max-idle=10
# 连接池中的最小空闲连接
spring.redis.jedis.pool.min-idle=2
# 连接超时时间（毫秒）
spring.redis.timeout=5000
```
## 2 获取token的两种方式
每一次进行幂等校验之前先获取token，因为token的时效性只有1次，我们每次获得的token在幂等操作后就无效了，所以一个token不需要长期保存在redis中。
### 2.1 主动发起http请求
- GET请求URI
idempotentToken/getToken.api
- GET参数
url: 需要幂等验证的URI(无需参数)
- 完整示例
http://fresh.nong12.com/idempotentToken/getToken.api?url=/role/listPage.action

### 2.2 服务端自动获取
在进入页面的Controller上添加注解`@Token`，参数为需要幂等验证的URI
示例代码:
```
@Token("/role/listPage.action")
@RequestMapping(value="/index.html", method = RequestMethod.GET)
public String index(ModelMap modelMap){...}
```

## 3. 发起幂等请求
请求时将token放入请求header或parameter
参数名为:token_value
示例代码:
```
//获取beetl中的token变量
${token_value}
```

## 4. 幂等验证
在需要验证的api接口上添加注解`@Idempotent`
- 验证header中的token(默认)
@Idempotent(Idempotent.HEADER)或@Idempotent
- 验证parameter中的token
@Idempotent(Idempotent.PARAMETER)

# 幂等的不足
- 增加了额外控制幂等的业务逻辑，复杂化了业务功能；
- 把并行执行的功能改为串行执行，降低了执行效率。

因此除了业务上的特殊要求外，尽量不提供幂等的接口。