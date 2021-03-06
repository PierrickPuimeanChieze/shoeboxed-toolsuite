package be.cleitech.receipt;

import be.cleitech.receipt.dropbox.DropboxService;
import be.cleitech.receipt.google.GoogleConfiguration;
import be.cleitech.receipt.shoeboxed.ShoeboxedRestResource;
import be.cleitech.receipt.shoeboxed.ShoeboxedService;
import be.cleitech.receipt.shoeboxed.domain.ProcessingState;
import com.dropbox.core.json.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;

/**
 * @author Pierrick Puimean-Chieze on 1/25/2017.
 */
@Import({GoogleConfiguration.class, ShoeboxedRestResource.class})

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableScheduling
public class Application {

    private final GoogleConfiguration googleConfiguration;
    @Value("${shoeboxed.redirectUrl}")
    String redirectUrl;
    @Value("${shoeboxed.clientId}")
    String clientId;
    @Value("${shoeboxed.clientSecret}")
    String clientSecret;

    @Value("${credentials.directory}/shoeboxedAccessToken")
    File shoeboxedAccesTokenTile;

    @Value("${shoeboxed.uploadProcessingState:NEEDS_SYSTEM_PROCESSING}")
    private ProcessingState shoeboxedProcessingStateForUpload;
    @Value("${processToOcr.uploadedDirName:uploaded}")
    private String shoeboxedUploadedDirName;
    @Value("${shoeboxed.username}")
    private String username;

    @Value("${shoeboxed.password}")
    private String password;

    @Autowired
    public Application(GoogleConfiguration googleConfiguration) {
        this.googleConfiguration = googleConfiguration;
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    /**
     * Property placeholder configurer needed to process @Value annotations
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ShoeboxedService shoeboxedService() throws IOException, JsonReader.FileLoadException {
        ShoeboxedService shoeboxedService = new ShoeboxedService(redirectUrl, clientId, clientSecret, shoeboxedProcessingStateForUpload, shoeboxedAccesTokenTile, username, password);

        shoeboxedService.initAccessToken();
        return shoeboxedService;
    }

    @Bean
    public DropboxService dropboxService(@Value("${dropbox.uploadPath}")
                                                 String uploadPath,

                                         @Value("${credentials.directory}/dropboxAcessToken")
                                                 File accessTokenFile
    ) {
        DropboxService dropboxService = new DropboxService(uploadPath, accessTokenFile);
        dropboxService.initDropboxAccessToken();
        return dropboxService;
    }


}

