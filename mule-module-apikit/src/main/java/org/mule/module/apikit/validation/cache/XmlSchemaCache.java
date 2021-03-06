/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit.validation.cache;

import org.mule.api.MuleContext;
import org.mule.api.registry.RegistrationException;
import org.mule.raml.interfaces.model.IRaml;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

import javax.xml.validation.Schema;

public final class XmlSchemaCache
{

    private static final String REGISTRY_XML_SCHEMA_CACHE_KEY_PREFIX = "__restRouterXmlSchemaCache__";

    public static LoadingCache<String, Schema> getXmlSchemaCache(MuleContext muleContext, String configId, IRaml api) throws RegistrationException
    {
        String cacheKey = REGISTRY_XML_SCHEMA_CACHE_KEY_PREFIX + configId;
        if (muleContext.getRegistry().get(cacheKey) == null)
        {
            LoadingCache<String, Schema> transformerCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build(new XmlSchemaCacheLoader(api));

            muleContext.getRegistry().registerObject(cacheKey, transformerCache);
        }

        return muleContext.getRegistry().get(cacheKey);
    }
}
