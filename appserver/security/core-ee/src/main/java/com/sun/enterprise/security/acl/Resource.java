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

package com.sun.enterprise.security.acl;

/**
 * Abstract resource ..
 * 
 * @author Harish Prabandham
 */
abstract public class Resource {
    private String app;
    private String name;
    private String method;

    protected Resource(String app, String name, String method) {
        this.app = app;
        this.name = name;
        this.method = method;
    }

    public String getName() {
        return this.name;
    }

    public String getApplication() {
        return this.app;
    }

    public String getMethod() {
        return this.method;
    }

    public int hashCode() {
        return getClass().hashCode();
    }

    public abstract boolean implies(Resource res);

    public abstract boolean equals(Object obj);

    public String toString() {
        return getApplication() + ":" + getName() + "." + getMethod();
    }
}
