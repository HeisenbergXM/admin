package com.company.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.company.admin.util.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDateTime;

@Configuration
public class MyBatisConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // sys_* 表时间字段
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                // sys_* 表用户字段（如有）
                this.strictInsertFill(metaObject, "createdBy", String.class, SecurityUtils.getCurrentUsername());
                this.strictInsertFill(metaObject, "updatedBy", String.class, SecurityUtils.getCurrentUsername());
                // VLM 阶段表时间字段（created_at / updated_at）
                this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // sys_* 表
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictUpdateFill(metaObject, "updatedBy", String.class, SecurityUtils.getCurrentUsername());
                // VLM 阶段表
                this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
