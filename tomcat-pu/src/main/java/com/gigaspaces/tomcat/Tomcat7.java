package com.gigaspaces.tomcat;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * A Tomcat 7 server bean compatible with XAP.  Note that the port
 * property is a starting point.  Depending on the number of instances
 * defined, and the ports available, the actual port may vary.
 * 
 * @author DeWayne
 *
 */
public class Tomcat7 {
	private static final Logger log=Logger.getLogger(Tomcat7.class.getName());
	private static Pattern pattern=Pattern.compile("^file:(.*)META-INF/spring/pu.xml$");
	private String appBase;
	private String contextPath;
	private int port;
	private int portRetries=10;
	private Webapp[] webapps;
	private Tomcat tomcat;

	public static void main(String[] args){
		Tomcat7 t=new Tomcat7();
		URL url=t.getClass().getClassLoader().getResource("META-INF/spring/pu.xml");
		Matcher matcher=pattern.matcher(url.toString());
		matcher.matches();
	}

	@PostConstruct
	public void startTomcat() throws ServletException, LifecycleException{
		Matcher matcher=pattern.matcher(this.getClass().getClassLoader().getResource("META-INF/spring/pu.xml").toString());
		matcher.matches();
		String base=matcher.group(1);

		int curport=this.port;
		int retries=0;
		//Retry until valid port found, or retries exhausted.
		while(true){
			tomcat = new Tomcat();

			tomcat.setBaseDir(base);
			tomcat.getHost().setAppBase(this.appBase);

			StandardServer server = (StandardServer)tomcat.getServer();
			AprLifecycleListener listener = new AprLifecycleListener();
			server.addLifecycleListener(listener);
			server.setParentClassLoader(getClass().getClassLoader());

			/* load webapps, if any */
			if(webapps!=null){
				for(Webapp app:webapps){
					tomcat.addWebapp(app.getName(),app.getPath());
				}
			}

			tomcat.setPort(curport);
			tomcat.start();
			if(tomcat.getConnector().getLocalPort()!=-1)break;

			log.severe(String.format("start failed on port %d, incrementing",curport));
			curport++;
			retries++;

			if(retries>=portRetries){
				log.severe("Unable to find an available port");
				break;
			}
			tomcat.stop();
			tomcat.destroy();
		}

	}

	@PreDestroy
	public void destroy()throws Exception{
		tomcat.stop();
		tomcat.destroy();
	}

	public String getAppBase() {
		return appBase;
	}
	@Required
	public void setAppBase(String appBase) {
		this.appBase = appBase;
	}
	public String getContextPath() {
		return contextPath;
	}
	@Required
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	public int getPort() {
		return port;
	}
	@Required
	public void setPort(int port) {
		this.port = port;
	}

	public void setWebapps(Webapp[] webapps) {
		this.webapps = webapps;
	}

	public Webapp[] getWebapps() {
		return webapps;
	}

	public void setPortRetries(int portRetries) {
		this.portRetries = portRetries;
	}

	public int getPortRetries() {
		return portRetries;
	}

}
