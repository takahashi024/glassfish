/*
 * Copyright (c) 2023, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.loader;

import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Manifest;

/**
 * Resource entry.
 *
 * @author Remy Maucherat 2007
 */
public class ResourceEntry {


    /**
     * The "last modified" time of the origin file at the time this class
     * was loaded, in milliseconds since the epoch.
     */
    public long lastModified = -1;


    /**
     * Binary content of the resource.
     */
    public byte[] binaryContent;


    /**
     * Loaded class.
     */
    public volatile Class<?> loadedClass;


    /**
     * URL source from where the object was loaded.
     */
    public URL source;


    /**
     * URL of the codebase from where the object was loaded.
     */
    public URL codeBase;


    /**
     * Manifest (if the resource was loaded from a JAR).
     */
    public Manifest manifest;


    /**
     * Certificates (if the resource was loaded from a JAR).
     */
    public Certificate[] certificates;
}

