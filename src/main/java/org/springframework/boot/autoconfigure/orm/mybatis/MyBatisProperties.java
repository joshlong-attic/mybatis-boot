package org.springframework.boot.autoconfigure.orm.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.util.Arrays;

/**
 *
 * @author Josh Long
 */
@ConfigurationProperties( prefix = "spring.mybatis")
public class MyBatisProperties {

    private Resource configLocation;

    private Resource[] mapperLocations;

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void setMapperLocations(Resource[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public Resource[] getMapperLocations() {
        return mapperLocations;
    }
}
