package cn.zdjc.wms.project.common.redis;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Spring Redis 工具类（增强日志与健壮性）
 *
 * @author ruoyi
 * @author [Your Name] - optimized with logging
 */
@AllArgsConstructor
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {

    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

    public RedisTemplate redisTemplate;

    public static final long DEFAULT_EXPIRE = 3600 * 24;
    public static final long NOT_EXPIRE = -1;

    // ==================== Value Operations ====================

    public <T> ValueOperations<String, T> setCacheObject(String key, T value) {
        try {
            ValueOperations<String, T> operation = redisTemplate.opsForValue();
            operation.set(key, value);
            log.debug("Redis SET - key: {}, value type: {}", key, value != null ? value.getClass().getSimpleName() : "null");
            return operation;
        } catch (Exception e) {
            log.error("Redis SET 失败 - key: {}", key, e);
            throw e;
        }
    }

    public <T> ValueOperations<String, T> setCacheObject(String key, T value, Long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, T> operation = redisTemplate.opsForValue();
            operation.set(key, value, timeout, timeUnit);
            log.debug("Redis SET with TTL - key: {}, timeout: {} {}, value type: {}",
                    key, timeout, timeUnit, value != null ? value.getClass().getSimpleName() : "null");
            return operation;
        } catch (Exception e) {
            log.error("Redis SET with TTL 失败 - key: {}", key, e);
            throw e;
        }
    }

    public <T> T getCacheObject(String key) {
        try {
            ValueOperations<String, T> operation = redisTemplate.opsForValue();
            T value = operation.get(key);
            log.debug("Redis GET - key: {}, hit: {}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Redis GET 失败 - key: {}", key, e);
            throw e;
        }
    }

    // ==================== Hash Operations ====================

    public void setMap(Object key, Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            log.warn("Redis HSET - key: {}, skip empty map", key);
            return;
        }
        try {
            redisTemplate.opsForHash().putAll(key, map);
            log.debug("Redis HSET - key: {}, fields: {}", key, map.size());
        } catch (Exception e) {
            log.error("Redis HSET 失败 - key: {}", key, e);
            throw e;
        }
    }

    // ==================== List Operations ====================

    public <T> ListOperations<String, T> setCacheList(String key, List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("Redis LPUSH - key: {}, skip empty list", key);
            return redisTemplate.opsForList();
        }
        try {
            ListOperations<String, T> listOperation = redisTemplate.opsForList();
            // 使用 rightPushAll (或 leftPushAll) 批量操作，性能更好
            listOperation.rightPushAll(key, dataList);
            log.debug("Redis LPUSH - key: {}, size: {}", key, dataList.size());
            return listOperation;
        } catch (Exception e) {
            log.error("Redis LPUSH 失败 - key: {}", key, e);
            throw e;
        }
    }

    public <T> List<T> getCacheList(String key) {
        try {
            ListOperations<String, T> listOperation = redisTemplate.opsForList();
            Long size = listOperation.size(key);
            if (size == null || size == 0) {
                log.debug("Redis LRANGE - key: {}, empty list", key);
                return Collections.emptyList();
            }
            // 一次性获取整个列表，避免多次 index 调用（性能优化）
            List<T> dataList = listOperation.range(key, 0, size - 1);
            log.debug("Redis LRANGE - key: {}, size: {}", key, dataList != null ? dataList.size() : 0);
            return dataList != null ? dataList : Collections.emptyList();
        } catch (Exception e) {
            log.error("Redis LRANGE 失败 - key: {}", key, e);
            throw e;
        }
    }

    // ==================== Set Operations ====================

    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
        if (dataSet == null || dataSet.isEmpty()) {
            log.warn("Redis SADD - key: {}, skip empty set", key);
            return redisTemplate.boundSetOps(key);
        }
        try {
            BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
            setOperation.add((T) dataSet.toArray());
            log.debug("Redis SADD - key: {}, size: {}", key, dataSet.size());
            return setOperation;
        } catch (Exception e) {
            log.error("Redis SADD 失败 - key: {}", key, e);
            throw e;
        }
    }

    public <T> Set<T> getCacheSet(String key) {
        try {
            BoundSetOperations<String, T> operation = redisTemplate.boundSetOps(key);
            Set<T> dataSet = operation.members();
            log.debug("Redis SMEMBERS - key: {}, size: {}", key, dataSet != null ? dataSet.size() : 0);
            return dataSet != null ? dataSet : Collections.emptySet();
        } catch (Exception e) {
            log.error("Redis SMEMBERS 失败 - key: {}", key, e);
            throw e;
        }
    }

    // ==================== Key Operations ====================

    public Collection<String> keys(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            log.debug("Redis KEYS - pattern: {}, found: {}", pattern, keys != null ? keys.size() : 0);
            return keys != null ? keys : Collections.emptySet();
        } catch (Exception e) {
            log.error("Redis KEYS 失败 - pattern: {}", pattern, e);
            throw e;
        }
    }

    public boolean existsKey(String key) {
        try {
            boolean exists = redisTemplate.hasKey(key);
            log.debug("Redis EXISTS - key: {}, exists: {}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("Redis EXISTS 失败 - key: {}", key, e);
            throw e;
        }
    }

    public void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Redis DEL - key: {}", key);
        } catch (Exception e) {
            log.error("Redis DEL 失败 - key: {}", key, e);
            throw e;
        }
    }

    public Boolean deleteKeyReturn(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Redis DEL (return) - key: {}, deleted: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis DEL (return) 失败 - key: {}", key, e);
            throw e;
        }
    }

    public void deleteKey(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        try {
            Set<String> keySet = keys.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            Long deleted = redisTemplate.delete(keySet);
            log.debug("Redis DEL batch - keys: {}, deleted: {}", keySet.size(), deleted);
        } catch (Exception e) {
            log.error("Redis DEL batch 失败 - keys count: {}", keys.size(), e);
            throw e;
        }
    }

    // ==================== Expiry Operations ====================

    public void expireKey(String key, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.expire(key, time, timeUnit);
            log.debug("Redis EXPIRE - key: {}, timeout: {} {}", key, time, timeUnit);
        } catch (Exception e) {
            log.error("Redis EXPIRE 失败 - key: {}", key, e);
            throw e;
        }
    }

    public void expireKeyAt(String key, Date date) {
        try {
            redisTemplate.expireAt(key, date);
            log.debug("Redis EXPIREAT - key: {}, at: {}", key, date);
        } catch (Exception e) {
            log.error("Redis EXPIREAT 失败 - key: {}", key, e);
            throw e;
        }
    }

    public long getExpire(String key, TimeUnit timeUnit) {
        try {
            long expire = redisTemplate.getExpire(key, timeUnit);
            log.debug("Redis TTL - key: {}, ttl: {} {}", key, expire, timeUnit);
            return expire;
        } catch (Exception e) {
            log.error("Redis TTL 失败 - key: {}", key, e);
            throw e;
        }
    }

    public void persistKey(String key) {
        try {
            redisTemplate.persist(key);
            log.debug("Redis PERSIST - key: {}", key);
        } catch (Exception e) {
            log.error("Redis PERSIST 失败 - key: {}", key, e);
            throw e;
        }
    }

    // ==================== Advanced ====================

    public int setNx(String key, int value, int timeSecond, TimeUnit timeUnit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeSecond, timeUnit);
            log.debug("Redis SETNX - key: {}, result: {}", key, result);
            return result != null && result ? 1 : 0;
        } catch (Exception e) {
            log.error("Redis SETNX 失败 - key: {}", key, e);
            return 0;
        }
    }

    // 可选：增加通用 setNx 支持泛型
    public <T> boolean setNx(String key, T value, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            log.debug("Redis SETNX (generic) - key: {}, success: {}", key, result);
            return result != null && result;
        } catch (Exception e) {
            log.error("Redis SETNX (generic) 失败 - key: {}", key, e);
            return false;
        }
    }
}