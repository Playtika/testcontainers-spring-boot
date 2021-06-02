package com.playtika.test.mongodb;

import com.playtika.test.common.properties.CommonContainerProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("embedded.mongodb")
public class MongodbProperties extends CommonContainerProperties {

    static final String BEAN_NAME_EMBEDDED_MONGODB = "embeddedMongodb";

    // https://hub.docker.com/_/mongo
    private String dockerImage = "mongo:4.4-bionic";
    private String host = "localhost";
    /**
     * The container internal port. Will be overwritten with mapped port.
     */
    private int port = 27017;
    private String username;
    private String password;
    private String database = "test";

}
