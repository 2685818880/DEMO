package cn.zdjc.wms.project.form.config;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import java.sql.SQLException;

@Configuration
public class MyBatisPlusConfig {

    @Autowired
    @Lazy
    private DatabaseConfigService databaseConfigService;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor() {
            @Override
            public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                                   ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
                String dbCode = DataSourceContextHolder.getDataSourceKeyOrDefault();
                if (!"default".equals(dbCode)) {
                    DatabaseConfigEntity config = databaseConfigService.getDatabaseConfigByCode(dbCode).orElse(null);
                    if (config != null) {
                        try {
                            DatabaseType dbType = DatabaseType.valueOf(config.getDbType().toUpperCase());
                            switch (dbType) {
                                case MYSQL: this.setDbType(DbType.MYSQL); break;
                                case SQLSERVER: this.setDbType(DbType.SQL_SERVER); break;
                                case ORACLE: this.setDbType(DbType.ORACLE); break;
                                default: this.setDbType(DbType.MYSQL);
                            }
                        } catch (IllegalArgumentException e) {
                            this.setDbType(DbType.MYSQL);
                        }
                    } else {
                        this.setDbType(DbType.MYSQL);
                    }
                } else {
                    this.setDbType(DbType.MYSQL);
                }
                super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
            }
        };
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
