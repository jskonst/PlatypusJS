/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.explorer.j2ee.tomcat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Data source JNDI resource.
 * Only required attributes supported yet.
 * @author vv
 */
public class DataSourceResource extends Resource {
    public static final String DATA_SOURCE_RESOURCE_TYPE_NAME = "javax.sql.DataSource";//NOI18N
    public static final String DRIVER_CLASS_NAME_ATTR_NAME = "driverClassName";//NOI18N
    public static final String URL_ATTR_NAME = "url";//NOI18N
    public static final String USER_NAME_ATTR_NAME = "username";//NOI18N
    public static final String PASSWORD_ATTR_NAME = "password";//NOI18N
    
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    
    /**
     * Gets the fully qualified Java class name of the JDBC driver to be used.
     * @return JDBC driver name
     */
    public String getDriverClassName() {
        return driverClassName;
    }
    
    /**
     * Sets the fully qualified Java class name of the JDBC driver to be used.
     * @param driverClassName JDBC driver name
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * Gets the database URL.
     * @return database URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Sets the database URL
     * @param url database URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the connection username to be passed to our JDBC driver to establish a connection.
     * @return connection username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the connection username to be passed to our JDBC driver to establish a connection.
     * @param username connection username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the connection password to be passed to our JDBC driver to establish a connection. 
     * @return connection password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the connection password to be passed to our JDBC driver to establish a connection. 
     * @param password connection password
     */
     public void setPassword(String password) {
        this.password = password;
    }
     
    @Override
     public void load(Element tag) {
        super.load(tag);
        driverClassName = tag.getAttribute(DRIVER_CLASS_NAME_ATTR_NAME);
        url = tag.getAttribute(URL_ATTR_NAME);
        username = tag.getAttribute(USER_NAME_ATTR_NAME);
        password = tag.getAttribute(PASSWORD_ATTR_NAME);
    }

    @Override
    public Element getElement(Document aDoc) {
        Element element = super.getElement(aDoc);
        if (driverClassName != null) {
            element.setAttribute(DRIVER_CLASS_NAME_ATTR_NAME, driverClassName);
        }
        if (url != null) {
            element.setAttribute(URL_ATTR_NAME, url);
        }
        if (username != null) {
            element.setAttribute(USER_NAME_ATTR_NAME, username);
        }
        if (password != null) {
            element.setAttribute(PASSWORD_ATTR_NAME, password);
        }
        return element;
    }
    
}
