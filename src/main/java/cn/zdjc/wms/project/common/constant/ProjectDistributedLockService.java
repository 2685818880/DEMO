package cn.zdjc.wms.project.common.constant;

import cn.zdjc.wms.project.ProjectConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 分布式锁服务，支持 Redis 分布式锁和单机 ReentrantLock 锁
 */
@Slf4j
@Service
public class ProjectDistributedLockService {

    /**
     * Redis 锁默认过期时间（毫秒），避免死锁
     */
    private static final long DEFAULT_LOCK_EXPIRE_TIME = 10 * 60 * 1000;

    private static final RedisScript<Long> RELEASE_LOCK_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );
    /**
     * Redis 锁标识值
     */
    private static final String LOCKED_VALUE = "locked";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 单机模式下使用的本地锁容器
     */
    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 获取一个带有超时时间的阻塞式锁（尝试等待直到获取锁或超时）
     *
     * @param key 锁的标识
     * @return 是否成功获取锁
     */
    public boolean acquireBlockedLock(String key) {
        if (ProjectConfig.MULTI_INSTANCE_MODE ) {
            // 分布式模式：Redis 实现
            long waitTime = ProjectConfig.DISTRIBUTE_LOCK_WAIT_TIME * 1000L;
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < waitTime) {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(key, LOCKED_VALUE, DEFAULT_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
                if (Boolean.TRUE.equals(acquired)) {
                    log.debug("Redis lock acquired for key: {}", key);
                    return true;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting for Redis lock", e);
                    return false;
                }
            }
            log.warn("Failed to acquire Redis lock for key: {}", key);
            return false;
        } else {
            // 单机模式：ReentrantLock 实现
            ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            try {
                boolean acquired = lock.tryLock(DEFAULT_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
                if (acquired) {
                    log.debug("Local lock acquired for key: {}", key);
                } else {
                    log.warn("Failed to acquire local lock for key: {}", key);
                }
                return acquired;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for local lock", e);
                return false;
            }
        }
    }

    /**
     * 快速失败方式获取锁（不等待）
     *
     * @param key 锁的标识
     * @return 是否成功获取锁
     */
    public boolean acquireFastFailedLock(String key) {
        if (ProjectConfig.MULTI_INSTANCE_MODE ) {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, LOCKED_VALUE, DEFAULT_LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            boolean result = Boolean.TRUE.equals(acquired);
            log.debug("Redis fast-fail lock for key: {} - {}", key, result ? "acquired" : "not acquired");
            return result;
        } else {
            ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            boolean acquired = lock.tryLock();
            log.debug("Local fast-fail lock for key: {} - {}", key, acquired ? "acquired" : "not acquired");
            return acquired;
        }
    }

    /**
     * 不设置过期时间的快速失败锁
     *
     * @param key 锁的标识
     * @return 是否成功获取锁
     */
    public boolean acquireUnexpiredFastFailedLock(String key) {
        if (ProjectConfig.MULTI_INSTANCE_MODE ) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, LOCKED_VALUE);
            boolean result = Boolean.TRUE.equals(acquired);
            log.debug("Redis unexpired fast-fail lock for key: {} - {}", key, result ? "acquired" : "not acquired");
            return result;
        } else {
            ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
            boolean acquired = lock.tryLock();
            log.debug("Local unexpired fast-fail lock for key: {} - {}", key, acquired ? "acquired" : "not acquired");
            return acquired;
        }
    }



    /**
     * 释放指定的锁
     *
     * @param key 锁的标识
     */
    public void releaseLock(String key) {
        if (ProjectConfig.MULTI_INSTANCE_MODE ) {
            // 分布式模式：使用 Lua 脚本保证原子性
            try {
                Long result = redisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(key), LOCKED_VALUE);
                if (result != null && result == 1L) {
                    log.debug("Redis lock released for key: {}", key);
                } else {
                    log.warn("Redis lock not released (not owned) for key: {}", key);
                }
            } catch (Exception e) {
                log.error("Error releasing Redis lock for key: {}", key, e);
            }
        } else {
            // 单机模式
            ReentrantLock lock = lockMap.remove(key);
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Local lock released for key: {}", key);
            } else {
                log.warn("Local lock not released or not held by current thread for key: {}", key);
            }
        }
    }
}