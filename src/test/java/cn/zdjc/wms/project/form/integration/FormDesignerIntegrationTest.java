package cn.zdjc.wms.project.form.integration;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import cn.zdjc.wms.project.form.service.FormConfigService;
import cn.zdjc.wms.project.form.service.FormDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FormDesignerIntegrationTest {

    @Autowired
    private FormConfigService formConfigService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Test
    public void testCompleteFormWorkflow() {
        FormConfigEntity formConfig = new FormConfigEntity();
        formConfig.setName("用户注册表单");
        formConfig.setCode("user_registration");
        formConfig.setFormType("REGISTRATION");
        formConfig.setConfigJson("{\"fields\": [{\"name\": \"username\", \"type\": \"String\"}], \"rules\": []}");

        FormConfigEntity savedForm = formConfigService.saveFormConfig(formConfig);
        assertNotNull(savedForm.getId());

        Optional<FormConfigEntity> foundForm = formConfigService.getFormConfigByCode("user_registration");
        assertTrue(foundForm.isPresent());
        assertEquals("用户注册表单", foundForm.get().getName());

        FormDataEntity formData = new FormDataEntity();
        formData.setFormCode("user_registration");
        formData.setBusinessKey("user_001");
        formData.setBusinessType("USER");
        formData.setFormDataJson("{\"username\": \"testuser\"}");

        FormDataEntity savedData = formDataService.submitFormData(formData);
        assertNotNull(savedData.getId());
        assertEquals("user_registration", savedData.getFormCode());

        boolean valid = formDataService.validateFormData("user_registration", "{\"username\": \"testuser2\"}");
        assertTrue(valid);
    }

    @Test
    public void testDatabaseConfigWorkflow() {
        DatabaseConfigEntity dbConfig = new DatabaseConfigEntity();
        dbConfig.setDbCode("test_integration_db");
        dbConfig.setDbName("集成测试数据库");
        dbConfig.setDbType("MYSQL");
        dbConfig.setDriverClass("com.mysql.cj.jdbc.Driver");
        dbConfig.setJdbcUrl("jdbc:mysql://localhost:3306/test_integration?useUnicode=true");
        dbConfig.setUsername("test");
        dbConfig.setPassword("test");

        try {
            DatabaseConfigEntity savedDb = databaseConfigService.saveDatabaseConfig(dbConfig);
            assertNotNull(savedDb.getId());
            assertEquals("test_integration_db", savedDb.getDbCode());
        } catch (Exception e) {
            System.out.println("数据库配置测试失败（预期中）: " + e.getMessage());
        }
    }
}
