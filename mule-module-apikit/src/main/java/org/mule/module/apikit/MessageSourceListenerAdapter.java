/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.mule.runtime.module.http.internal.listener.DefaultHttpListener;
import org.mule.runtime.module.http.internal.listener.DefaultHttpListenerConfig;

public class MessageSourceListenerAdapter //implements IMessageSource
{

    private DefaultHttpListener listener;
    private DefaultHttpListenerConfig config;

    public MessageSourceListenerAdapter(DefaultHttpListener messageSource)
    {
        listener = messageSource;
        config = (DefaultHttpListenerConfig) messageSource.getConfig();
    }

    //@Override
    public String getAddress()
    {
        return String.format("%s://%s:%s%s", getScheme(), config.getHost(), config.getPort(), getPath());
    }

    //@Override
    public String getPath()
    {
        String path = listener.getPath();
        return path.endsWith("/*") ? path.substring(0, path.length() - 2) : path;
    }

    //@Override
    public String getScheme()
    {
        return config.getTlsContext() != null ? "https" : "http";
    }
}
