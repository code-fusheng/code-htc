package xyz.fusheng.code.htc.plugin.orm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.IFill;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import xyz.fusheng.code.springboot.core.entity.BaseEntity;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

import static com.baomidou.mybatisplus.generator.config.rules.DbColumnType.INTEGER;

// @Configuration
//@ConditionalOnProperty(prefix = "mybatis-plus.generator", name = "enable", havingValue = "true", matchIfMissing = true)
public class MybatisPlusGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MybatisPlusGenerator.class);

    final static String  DIR_PATH = System.getProperty("user.dir");
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${mybatis-plus.generator.enable:true}")
    private Boolean enable;
    @Value("${mybatis-plus.generator.parent:xyz.fusheng}")
    private String parent;
    @Value("${mybatis-plus.generator.moduleName:code.htc}")
    private String moduleName;

    @PostConstruct
    public void doGenerator() {
        logger.info("===> 开始初始化实体对象");
        if (enable) {
            DataSourceConfig.Builder dataSourceConfig = new DataSourceConfig.Builder(url, username, password)
                    .typeConvert((globalConfig, fieldType) -> {
                        String type = fieldType.toLowerCase();
                        if (type.contains("tinyint") || type.contains("tinyint(1)") || type.contains("tinyint(2)") || type.contains("tinyint(4)")) {
                            return INTEGER;
                        }
                        if (type.contains("bit")) {
                            return INTEGER;
                        }
                        return new MySqlTypeConvert().processTypeConvert(globalConfig, fieldType);
                    })
                    .keyWordsHandler(new MySqlKeyWordsHandler());

            List<IFill> fillList = new ArrayList<>();
            fillList.add(new Property("creatorId", FieldFill.INSERT));
            fillList.add(new Property("updaterId", FieldFill.INSERT_UPDATE));
            fillList.add(new Property("creatorName", FieldFill.INSERT));
            fillList.add(new Property("updaterName", FieldFill.INSERT_UPDATE));

            FastAutoGenerator.create(dataSourceConfig)
                    .globalConfig(builder -> builder
                            //.fileOverride()
                            .disableOpenDir()
                            .outputDir(DIR_PATH + "/src/main/java")
                            .author("code-fusheng")
                            .enableSwagger()
                            .dateType(DateType.ONLY_DATE)
                            .build())
                    .packageConfig(builder -> builder
                            .parent(parent)
                            .moduleName(moduleName)
                            .controller("controller")
                            .entity("model.entity")
                            .service("core.service")
                            .serviceImpl("core.service.impl")
                            .mapper("core.mapper")
                            .xml(new ClassPathResource("mapper").getPath())
                            .build())
                    .strategyConfig(builder -> builder
                            .addInclude("patrol_berth", "patrol_device")
                            .entityBuilder()
                            .superClass(BaseEntity.class)
                            .enableColumnConstant()
                            .enableLombok()
                            //.versionColumnName("version").versionPropertyName("version")
                            .logicDeleteColumnName("is_deleted").logicDeletePropertyName("isDeleted")
                            .naming(NamingStrategy.underline_to_camel)
                            .addTableFills(fillList)
                            .idType(IdType.AUTO)
                            .controllerBuilder()
                            .enableRestStyle().formatFileName("%sController")
                            .mapperBuilder().superClass(BaseMapper.class)
                            .enableMapperAnnotation()
                            .build())
                    .templateEngine(new VelocityTemplateEngine())
                    .execute();
        }
    }

    @PreDestroy
    public void doDestroy() {

    }

}