/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.security.jauth;

import javax.security.auth.callback.CallbackHandler;

/**
 * This class manages the configuration AuthModules.
 * 
 * <p>
 * An AuthModule represents a pluggable component for performing security-related request and response processing, and
 * can be configured for a particular interception point and provider ID. The provider ID is an administrator-defined
 * value. The standard interception points include:
 *
 * <ul>
 * <li>HTTP
 * <li>EJB
 * <li>SOAP
 * </ul>
 *
 * <p>
 * Information may be associated with a configured module, including its fully qualified class name (so it can be
 * instantiated), and module options (which help tune the behavior of the module). It is the responsibility of the
 * AuthConfig implementation to load any required module information.
 *
 * <p>
 * Callers do not operate on AuthModules directly. Instead they rely on a ClientAuthContext or ServerAuthContext to
 * manage the invocation of modules. A caller obtains an instance of ClientAuthContext or ServerAuthContext by calling
 * the <code>getClientAuthContext</code> or <code>getServerAuthContext</code> method, respectively. Each method takes as
 * arguments an <i>intercept</i>, an <i>id</i>, a <i>requestPolicy</i>, and a <i>responsePolicy</i>.
 *
 * <p>
 * An AuthConfig implementation determines the modules to be invoked via the <i>intercept</i> and <i>id</i> values. It
 * then encapsulates those modules in a ClientAuthContext or ServerAuthContext instance, and returns that instance. The
 * returned object is responsible for instantiating, initializing, and invoking the configured modules (when called
 * upon).
 *
 * <p>
 * The module initializion step involves calling each configured module's <code>AuthModule.initialize</code> method. The
 * received <i>requestPolicy</i> and <i>responsePolicy</i> are passed to this method. It is then the modules'
 * responsibility, when invoked, to enforce these policies.
 *
 * <p>
 * A system-wide AuthConfig instance can be retrieved by invoking <code>getConfig</code>. A default implementation is
 * provided, and can be replaced by setting the value of the "authconfig.provider" security property (in the Java
 * security properties file) to the fully qualified name of the desired implementation class. The Java security
 * properties file is located in the file named &lt;JAVA_HOME&gt;/lib/security/java.security, where &lt;JAVA_HOME&gt;
 * refers to the directory where the JDK was installed.
 *
 * @version %I%, %G%
 * @see ClientAuthContext
 * @see ServerAuthContext
 */
public abstract class AuthConfig {

    /**
     * HTTP interception point.
     */
    public static final String HTTP = "HTTP";

    /**
     * EJB interception point.
     */
    public static final String EJB = "EJB";

    /**
     * SOAP interception point.
     */
    public static final String SOAP = "SOAP";

    // security property to replace default AuthConfig implementation
    private static final String AUTHCONFIG_PROPERTY = "authconfig.provider";

    // class name of default AuthConfig implementation
    private static final String DEFAULT_CLASS = "com.sun.enterprise.security.jauth.ConfigFile";

    private static AuthConfig config;

    // package private for ConfigFile
    static ClassLoader getClassLoader() {

        final ClassLoader rvalue;

        rvalue = (ClassLoader) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        return rvalue;
    };

    /**
     * Sole constructor. (For invocation by subclass constructors, typically implicit.)
     */
    protected AuthConfig() {
    }

    /**
     * Get a system-wide module configuration.
     *
     * <p>
     * If an AuthConfig object was set via the <code>setAuthConfig</code> method, then that object is returned. Otherwise,
     * an instance of the AuthConfig object configured in the <i>authconfig.provider</i> security property is returned. If
     * that property is not set, a default implementation is returned.
     *
     * @return a system-wide AuthConfig instance.
     *
     * @exception SecurityException if the caller does not have permission to retrieve the configuration.
     */
    public static synchronized AuthConfig getAuthConfig() {
        /**
         * XXX security check? SecurityManager sm = System.getSecurityManager(); if (sm != null) sm.checkPermission(new
         * AuthPermission("getAuthConfig"));
         */

        if (config == null) {
            String config_class = null;
            config_class = (String) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    return java.security.Security.getProperty(AUTHCONFIG_PROPERTY);
                }
            });
            if (config_class == null) {
                config_class = DEFAULT_CLASS;
            }

            try {
                final String finalClass = config_class;
                config = (AuthConfig) java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
                        return Class.forName(finalClass, true, getClassLoader()).newInstance();
                    }
                });
            } catch (java.security.PrivilegedActionException e) {
                throw (SecurityException) new SecurityException().initCause(e.getException());
            }
        }
        return config;
    }

    /**
     * Set a system-wide module configuration.
     *
     * @param config the new configuration.
     *
     * @exception SecurityException if the caller does not have permission to set the configuration.
     */
    public static void setAuthConfig(AuthConfig config) {
        /**
         * XXX security check? SecurityManager sm = System.getSecurityManager(); if (sm != null) { sm.checkPermission(new
         * AuthPermission("setAuthConfig")); }
         */

        AuthConfig.config = config;
    }

    /**
     * Get a ClientAuthContext.
     *
     * <p>
     * The modules configured for the returned ClientAuthContext are determined by the <i>intercept</i> and provider
     * <i>id</i> input parameters. The returned ClientAuthContext may be null, which signifies that there are no modules
     * configured.
     *
     * <p>
     * The returned ClientAuthContext encapsulates both the configured modules, as well as the module invocation semantics
     * (for example the order modules are to be invoked, and whether certain modules must succeed). Individual
     * ClientAuthContext implementations may enforce custom module invocation semantics.
     *
     * @param intercept      the interception point used to determine the modules configured for the returned
     *                       ClientAuthContext. Standard values include:
     *                       <ul>
     *                       <li>HTTP
     *                       <li>EJB
     *                       <li>SOAP
     *                       </ul>
     *
     * @param id             the provider id used to determine the modules configured for the returned ClientAuthContext, or
     *                       null. If null, a default ID may be used.
     *
     * @param requestPolicy  the application request policy to be enfored by the modules, or null. If null, a default
     *                       request policy may be used.
     *
     * @param responsePolicy the application response policy to be enfored by the modules, or null. If null, a default
     *                       response policy may be used.
     *
     * @param handler        the CallbackHandler to associate with the returned ClientAuthContext for use by configured
     *                       modules to request information from the caller, or null. If null, a default handler may be
     *                       used.
     *
     * @return a ClientAuthContext, or null.
     */
    public abstract ClientAuthContext getClientAuthContext(String intercept, String id, AuthPolicy requestPolicy, AuthPolicy responsePolicy,
            CallbackHandler handler) throws AuthException;

    /**
     * Get a ServerAuthContext.
     *
     * <p>
     * The modules configured for the returned ServerAuthContext are determined by the <i>intercept</i> and provider
     * <i>id</i>, input parameters. The returned ServerAuthContext may be null, which signifies that there are no modules
     * configured.
     *
     * <p>
     * The returned ServerAuthContext encapsulates both the configured modules, as well as the module invocation semantics
     * (for example the order modules are to be invoked, and whether certain modules must succeed). Individual
     * ServerAuthContext implementations may enforce custom module invocation semantics.
     *
     * @param intercept      the interception point used to determine the modules configured for the returned
     *                       ServerAuthContext. Standard values include:
     *                       <ul>
     *                       <li>HTTP
     *                       <li>EJB
     *                       <li>SOAP
     *                       </ul>
     *
     * @param id             the provider id used to determine the modules configured for the returned ClientAuthContext, or
     *                       null. If null, a default id may be used.
     *
     * @param requestPolicy  the application request policy to be enfored by the modules, or null. If null, a default
     *                       request policy may be used.
     *
     * @param responsePolicy the application response policy to be enfored by the modules, or null. If null, a default
     *                       response policy may be used.
     *
     * @param handler        the CallbackHandler to associate with the returned ClientAuthContext, which can be used by
     *                       configured modules to request information from the caller, or null. If null, a default handler
     *                       may be used.
     *
     * @return a ServerAuthContext, or null.
     */
    public abstract ServerAuthContext getServerAuthContext(String intercept, String id, AuthPolicy requestPolicy, AuthPolicy responsePolicy,
            CallbackHandler handler) throws AuthException;

    /**
     * Refresh the internal representation of the active configuration by re-reading the provider configs.
     */
    public abstract void refresh() throws AuthException;
}
