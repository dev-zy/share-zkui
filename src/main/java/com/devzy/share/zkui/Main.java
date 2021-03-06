package com.devzy.share.zkui;

import java.io.File;
import java.util.Date;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.h2.store.fs.FileUtils;
import org.slf4j.LoggerFactory;

import com.devzy.share.zkui.dao.Dao;
import com.devzy.share.zkui.utils.PropertiesConfigUtil;
public class Main {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
    	String DEFAULT_PATH = Main.class.getResource("/").getPath();
    	String BASEDIR = System.getProperty("basedir");
    	String BASE_PATH = BASEDIR!=null?BASEDIR:System.getProperty("base.path",DEFAULT_PATH);
        logger.info("Starting ZKUI!"+BASE_PATH);
        String conf = BASE_PATH+"/application.properties";
        if(!FileUtils.exists(conf)) {
        	conf = BASE_PATH+"/config/application.properties";
        }
        logger.info("conf:"+conf);
        PropertiesConfiguration globalProps = PropertiesConfigUtil.getConfig(conf);
        if(globalProps==null) {
        	logger.info("Please create config.cfg properties file and then execute the program!");
            System.exit(1);
        }
//        if("windows".equalsIgnoreCase(System.getProperty("sun.desktop",""))&&BASE_PATH.startsWith("/")&&BASE_PATH.charAt(2)==':') {
//        	BASE_PATH = BASE_PATH.substring(1);
//        }
        globalProps.setProperty("uptime", new Date().toString());
        new Dao().checkNCreate();
        Server server = new Server();
        
        String webapp ="webapp";
        WebAppContext servletContextHandler = new WebAppContext();
        servletContextHandler.setContextPath("/");
        servletContextHandler.setResourceBase(webapp);
        ClassList clist = ClassList.setServerDefault(server);
        clist.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
        servletContextHandler.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*(/target/classes/|.*.jar)");
        servletContextHandler.setParentLoaderPriority(true);
        servletContextHandler.setInitParameter("useFileMappedBuffer", "false");
        servletContextHandler.setMaxFormContentSize(Integer.MAX_VALUE);
        servletContextHandler.setMaxFormKeys(Integer.MAX_VALUE);
        
        ResourceHandler staticResourceHandler = new ResourceHandler();
        staticResourceHandler.setDirectoriesListed(false);
        Resource staticResources = Resource.newClassPathResource(webapp);
        staticResourceHandler.setBaseResource(staticResources);
        staticResourceHandler.setWelcomeFiles(new String[]{"html/index.html"});

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{staticResourceHandler, servletContextHandler});
        server.setHandler(handlers); 
        String port = System.getenv("SERVER_PORT");
        if(StringUtils.isBlank(port)) {
        	port = globalProps.getString("server.port","9090");
        }
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(Integer.parseInt(port));
        String httpsEnabled = System.getenv("https_enabled");
        if(StringUtils.isBlank(port)) {
        	httpsEnabled = globalProps.getString("https.enabled","false");
        }
        if (httpsEnabled.equals("true")) {
        	String file = System.getenv("KEYSTORE_FILE");
            if(StringUtils.isBlank(port)) {
            	file = globalProps.getString("keystore.file");
            }
            String pwd = System.getenv("KEYSTORE_PWD");
            if(StringUtils.isBlank(port)) {
            	pwd = globalProps.getString("keystore.pwd");
            }
            String mpwd = System.getenv("KEYSTORE_PWD");
            if(StringUtils.isBlank(port)) {
            	mpwd = globalProps.getString("keystore.manager.pwd");
            }
            File keystoreFile = new File(file);
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
            sslContextFactory.setKeyStorePassword(pwd);
            sslContextFactory.setKeyManagerPassword(mpwd);
            HttpConfiguration HTTPS_CONFIG = new HttpConfiguration(http_config);
            HTTPS_CONFIG.addCustomizer(new SecureRequestCustomizer());
            ServerConnector https = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(HTTPS_CONFIG));
            https.setPort(Integer.parseInt(port));
            server.setConnectors(new Connector[]{https});
        } else {
            if(globalProps.getProperty("X-Forwarded-For").equals("true")) {
                http_config.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());
            }
            ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
            http.setPort(Integer.parseInt(port));
            server.setConnectors(new Connector[]{http});
        }
        server.start();
        server.join();
    }

}
