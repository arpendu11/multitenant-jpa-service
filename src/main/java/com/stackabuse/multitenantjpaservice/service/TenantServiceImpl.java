package com.stackabuse.multitenantjpaservice.service;

import com.stackabuse.multitenantjpaservice.dto.TenantRegistrationDTO;
import com.stackabuse.multitenantjpaservice.entity.Tenant;
import com.stackabuse.multitenantjpaservice.repository.TenantRepository;
import com.stackabuse.multitenantjpaservice.util.EncryptionUtil;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@EnableConfigurationProperties(LiquibaseProperties.class)
public class TenantServiceImpl implements TenantService {

    private final EncryptionUtil encryptionService;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final LiquibaseProperties liquibaseProperties;
    private final ResourceLoader resourceLoader;
    private final TenantRepository tenantRepository;

    private final String urlPrefix;
    private final String secret;
    private final String salt;

    @Autowired
    public TenantServiceImpl(EncryptionUtil encryptionService,
                                       DataSource dataSource,
                                       JdbcTemplate jdbcTemplate,
                                       @Qualifier("tenantLiquibaseProperties")
                                         LiquibaseProperties liquibaseProperties,
                                       ResourceLoader resourceLoader,
                                       TenantRepository tenantRepository,
                                       @Value("${multitenancy.tenant.datasource.url-prefix}")
                                               String urlPrefix,
                                       @Value("${encryption.secret}")
                                               String secret,
                                       @Value("${encryption.salt}")
                                               String salt
    ) {
        this.encryptionService = encryptionService;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.liquibaseProperties = liquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.tenantRepository = tenantRepository;
        this.urlPrefix = urlPrefix;
        this.secret = secret;
        this.salt = salt;
    }

    private static final String VALID_SCHEMA_NAME_REGEXP = "^[a-z0-9]{1,8}$";

    @Override
    public Tenant registerTenant(TenantRegistrationDTO tenantRegistrationDTO) throws Exception {

        log.info("Check if the Tenant already exists");
        String tenantKey = tenantRegistrationDTO.getKey();
        // The schema 'public' by default will be present and 'public' tenant creation is not allowed
        if (Objects.nonNull(findTenant(tenantKey)) || tenantKey.equals("public")) {
            throw new Exception(MessageFormat.format("Tenant with the key {0} already exists in the system", tenantKey));
        }

        log.info("Verify tenant key string to prevent SQL injection: {}", tenantKey);
        if (!tenantKey.matches(VALID_SCHEMA_NAME_REGEXP)) {
            throw new Exception(MessageFormat.format("Invalid schema name: {}",  tenantKey));
        }

        log.info("Convert Tenant DTO to Tenant data object: {}", tenantRegistrationDTO);
        Tenant newTenant = new Tenant();
        newTenant.setTenantId(tenantRegistrationDTO.getTenantId());
        newTenant.setKey(tenantRegistrationDTO.getKey());
        newTenant.setUrl(urlPrefix + tenantRegistrationDTO.getKey());
        newTenant.setPassword(encryptionService.encrypt(tenantRegistrationDTO.getPassword(), secret, salt));
        newTenant.setEnabled(tenantRegistrationDTO.getEnabled());
        newTenant.setCreatedBy("admin");
        newTenant.setCreatedOn(System.currentTimeMillis());
        newTenant.setLastUpdatedBy("admin");
        newTenant.setLastUpdatedOn(System.currentTimeMillis());

        log.info("Create database for the tenant: {}", newTenant);
        try {
            createDatabase(tenantRegistrationDTO.getKey(), tenantRegistrationDTO.getPassword());
        } catch (DataAccessException e) {
            throw new Exception(MessageFormat.format("Error when creating db: {}", tenantRegistrationDTO.getKey()));
        }
        try (Connection connection = DriverManager.getConnection(
                urlPrefix + tenantRegistrationDTO.getKey(),
                tenantRegistrationDTO.getKey(),
                tenantRegistrationDTO.getPassword())) {
            DataSource tenantDataSource = new SingleConnectionDataSource(connection, false);
            runLiquibase(tenantDataSource);
        } catch (SQLException | LiquibaseException e) {
            throw new Exception(MessageFormat.format("Error when populating db: {}", tenantRegistrationDTO.getKey()));
        }

        log.info("Save tenant object to the global namespace: {}", newTenant);
        return tenantRepository.save(newTenant);
    }

    private void createDatabase(String db, String password) {
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute("CREATE DATABASE " + db));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute(
                "CREATE USER " + db + " WITH ENCRYPTED PASSWORD '" + password + "'"));
        jdbcTemplate.execute((StatementCallback<Boolean>) stmt -> stmt.execute(
                "GRANT ALL PRIVILEGES ON DATABASE " + db + " TO " + db));
    }

    private void runLiquibase(DataSource dataSource) throws LiquibaseException {
        SpringLiquibase liquibase = getSpringLiquibase(dataSource);
        liquibase.afterPropertiesSet();
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
        liquibase.setLiquibaseTablespace(liquibaseProperties.getLiquibaseTablespace());
        liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setShouldRun(liquibaseProperties.isEnabled());
        liquibase.setLabels(liquibaseProperties.getLabels());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        liquibase.setRollbackFile(liquibaseProperties.getRollbackFile());
        liquibase.setTestRollbackOnUpdate(liquibaseProperties.isTestRollbackOnUpdate());
        return liquibase;
    }

    @Override
    public boolean isTenantExists(String tenantKey) {
        // check if the schema exists.
        return tenantRepository.findByKey(tenantKey).isPresent();
    }


    @Override
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @Override
    public Boolean deactivateTenant(String tenantKey) throws Exception {
        log.info("Check if the Tenant already exists: {}", tenantKey);
        Tenant tenant = findTenant(tenantKey);
        if (Objects.isNull(tenant)) {
            throw new Exception(MessageFormat.format("Tenant with the key {0} does not exist in the system", tenantKey));
        }
        tenant.setEnabled(false);
        tenant.setCreatedBy("admin");
        tenant.setCreatedOn(System.currentTimeMillis());
        tenant.setLastUpdatedBy("admin");
        tenant.setLastUpdatedOn(System.currentTimeMillis());
        tenantRepository.save(tenant);
        return true;
    }

    @Override
    public Boolean activateTenant(String tenantKey) throws Exception {
        log.info("Check if the Tenant already exists");
        Tenant tenant = findTenant(tenantKey);
        if (Objects.isNull(tenant)) {
            throw new Exception(MessageFormat.format("Tenant with the key {} doesn't exist in the system", tenantKey));
        }
        tenant.setEnabled(true);
        tenant.setCreatedBy("admin");
        tenant.setCreatedOn(System.currentTimeMillis());
        tenant.setLastUpdatedBy("admin");
        tenant.setLastUpdatedOn(System.currentTimeMillis());
        tenantRepository.save(tenant);
        return true;
    }

    @Override
    public Tenant findTenant(String key) {
        return tenantRepository.findByKey(key).orElse(null);
    }
}
