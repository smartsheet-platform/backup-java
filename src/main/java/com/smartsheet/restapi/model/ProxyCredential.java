package com.smartsheet.restapi.model;

public class ProxyCredential {

    public String username;
    public String password;
    public String proxyHost;
    public int port;

    public ProxyCredential(String username, String password, String proxyHost, int port) {
        this.username = username;
        this.password = password;
        this.proxyHost = proxyHost;
        this.port = port == 0 ? 8080 : port;
    }

}
