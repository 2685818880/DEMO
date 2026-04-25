package cn.zdjc.wms.project.form.aop;

import cn.zdjc.wms.project.form.config.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class PerformanceMonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorAspect.class);
    private static final long SLOW_THRESHOLD_MS = 1000;

    @Around("execution(* cn.zdjc.wms.project.form.service.*.*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            if (executionTime > SLOW_THRESHOLD_MS) {
                String currentDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
                logger.warn("慢服务调用: {}.{}, 数据源: {}, 耗时: {}ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    currentDataSource,
                    executionTime);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("服务调用: {}.{}, 耗时: {}ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            }
        }
    }

    @Around("@annotation(cn.zdjc.wms.project.form.annotation.FormDataSource)")
    public Object monitorDataSourceSwitchPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String beforeDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
        try {
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            String afterDataSource = DataSourceContextHolder.getDataSourceKeyOrDefault();
            long switchTime = stopWatch.getTotalTimeMillis();
            if (switchTime > 100) {
                logger.warn("数据源切换耗时较长: {} -> {}, 耗时: {}ms",
                    beforeDataSource, afterDataSource, switchTime);
            }
        }
    }
}
