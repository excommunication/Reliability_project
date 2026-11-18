package app.config;


import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class TomcatConfigLogger implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            ProtocolHandler protocol = connector.getProtocolHandler();

            if (protocol instanceof AbstractProtocol<?> abstractProtocol) {
                System.out.println("Max threads:        " + abstractProtocol.getMaxThreads());
                System.out.println("Min spare threads:  " + abstractProtocol.getMinSpareThreads());
                System.out.println("Accept count:       " + abstractProtocol.getAcceptCount());
            }
        });
    }
}