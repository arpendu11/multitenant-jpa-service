package com.stackabuse.multitenantjpaservice.service;

import com.stackabuse.multitenantjpaservice.dto.TenantRegistrationDTO;
import com.stackabuse.multitenantjpaservice.entity.Tenant;
import com.stackabuse.multitenantjpaservice.repository.TenantRepository;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@Service
public class TenantServiceImpl implements TenantService {

    private final LiquibaseProperties liquibaseProperties;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final TenantRepository tenantRepository;
    private final ResourceLoader resourceLoader;

    private static final String VALID_SCHEMA_NAME_REGEXP = "^[a-z0-9]{1,8}$";

    @Autowired
    public TenantServiceImpl(DataSource dataSource,
                             JdbcTemplate jdbcTemplate,
                             @Qualifier("tenantLiquibaseProperties")
                                     LiquibaseProperties liquibaseProperties,
                             ResourceLoader resourceLoader,
                             TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.liquibaseProperties = liquibaseProperties;
        this.resourceLoader = resourceLoader;
        this.tenantRepository = tenantRepository;
    }

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
        newTenant.setEnabled(tenantRegistrationDTO.getEnabled());
        newTenant.setCreatedBy("admin");
        newTenant.setCreatedOn(System.currentTimeMillis());
        newTenant.setLastUpdatedBy("admin");
        newTenant.setLastUpdatedOn(System.currentTimeMillis());

        log.info("Create tenant schema namespace for the tenant: {}", newTenant);
        try {
            createSchema(tenantRegistrationDTO.getKey());
            runLiquibase(dataSource, tenantRegistrationDTO.getKey());
        } catch (DataAccessException e) {
            throw new Exception(MessageFormat.format("Error when creating schema: {}", tenantRegistrationDTO.getKey()));
        } catch (LiquibaseException e) {
            throw new Exception(MessageFormat.format("Error when populating schema: {}", tenantRegistrationDTO.getKey()));
        }

        log.info("Save tenant object to the global namespace: {}", newTenant);
        return tenantRepository.save(newTenant);
    }

    private Boolean createSchema(String schema) {
        return jdbcTemplate.execute((StatementCallback<Boolean>) stmt ->
                stmt.execute("CREATE SCHEMA " + "\"" + schema + "\""));
    }

    private void runLiquibase(DataSource dataSource, String schema) throws LiquibaseException {
        SpringLiquibase liquibase = getSpringLiquibase(dataSource, schema);
        liquibase.afterPropertiesSet();
    }

    protected SpringLiquibase getSpringLiquibase(DataSource dataSource, String schema) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema(schema);
        liquibase.setChangeLog(liquibaseProperties.getChangeLog());
        liquibase.setContexts(liquibaseProperties.getContexts());
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
        // check the schema is exists.
        String schemaQuery = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ?";

        try {
            String tenant = jdbcTemplate.queryForObject(schemaQuery, String.class, new Object[] { tenantKey });
            return Objects.nonNull(tenant);
        } catch (EmptyResultDataAccessException e) {
            log.warn(String.format("The tenant '%s' doesn't exist in the system ", tenantKey));
            return false;
        }
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
