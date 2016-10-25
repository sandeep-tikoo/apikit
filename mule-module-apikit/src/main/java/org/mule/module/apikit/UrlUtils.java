/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

//import static org.mule.compatibility.transport.http.HttpConnector.HTTP_CONTEXT_PATH_PROPERTY;
//import static org.mule.compatibility.transport.http.HttpConnector.HTTP_REQUEST_PATH_PROPERTY;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.Event;
import org.mule.module.apikit.exception.ApikitRuntimeException;
import org.mule.runtime.core.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils
{
    public static final String HTTP_CONTEXT_PATH_PROPERTY = "http.context.path";
    public static final String HTTP_REQUEST_PATH_PROPERTY = "http.request.path";

    public static String getBaseSchemeHostPort(Event event)
    {
        String host = event.getMessage().getInboundProperty("host");
        String chHost = System.getProperty("fullDomain");
        if (chHost != null)
        {
            host = chHost;
        }
        return getScheme(event.getMessage()) + "://" + host;
    }

    public static String getScheme(Message message)
    {
        String scheme = ((HttpRequestAttributes)message.getAttributes()).getScheme();
        if (scheme == null)
        {
            String endpoint = ((HttpRequestAttributes)message.getAttributes()).getRequestUri(); //TODO CHECK IF THIS IS THE CORRECT PROPERTY//.getInboundProperty("http.context.uri");
            if (endpoint == null)
            {
                throw new ApikitRuntimeException("Cannot figure out the request scheme");
            }
            if (endpoint.startsWith("http:"))
            {
                scheme = "http";
            }
            else if (endpoint.startsWith("https:"))
            {
                scheme = "https";
            }
            else
            {
                throw new ApikitRuntimeException("Unsupported scheme: " + endpoint);
            }
        }
        return scheme;
    }

    public static String getBaseSchemeHostPort(String baseUri)
    {
        URL url;
        try
        {
            url = new URL(baseUri);
        }
        catch (MalformedURLException e)
        {
            return "http://localhost";
        }
        return url.getProtocol() + "://" + url.getAuthority();
    }

    public static String getResourceRelativePath(Message message)
    {
        String path = ((HttpRequestAttributes)message.getAttributes()).getRequestPath();
        //String basePath = getBasePath(message);
        //path = path.substring(basePath.length());
        if (!path.startsWith("/") && !path.isEmpty())
        {
            path = "/" + path;
        }
        return path;
    }

    private static int getEndOfBasePathIndex(String baseAndApiPath, String requestPath)
    {
        int amountOfSlashesInBasePath = 0;
        for (int i = 0; i < baseAndApiPath.length(); i++)
        {
            if (Character.compare(baseAndApiPath.charAt(i),'/') == 0)
            {
                amountOfSlashesInBasePath++;
            }
        }
        int amountOfSlashesInRequestPath = 0;
        int character = 0;
        for (; character < requestPath.length() && amountOfSlashesInRequestPath < amountOfSlashesInBasePath; character++)
        {
            if (Character.compare(requestPath.charAt(character),'/') == 0)
            {
                amountOfSlashesInRequestPath++;
            }
        }

        return character;
    }
    public static String getRelativePath(Message message)
    {
        String baseAndApiPath = ((HttpRequestAttributes)message.getAttributes()).getListenerPath();
        String requestPath = ((HttpRequestAttributes)message.getAttributes()).getRequestPath();

        int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
        String relativePath = requestPath.substring(character);
        for(; character > 0 && Character.compare(requestPath.charAt(character - 1),'/') == 0; character--)
        {
            relativePath = "/" + relativePath;
        }
        return relativePath;
    }

    public static String getBasePath(Message message)
    {
        String baseAndApiPath = ((HttpRequestAttributes)message.getAttributes()).getListenerPath();
        String requestPath = ((HttpRequestAttributes)message.getAttributes()).getRequestPath();
        int character = getEndOfBasePathIndex(baseAndApiPath, requestPath);
        return requestPath.substring(0, character);
    }

    public static String getQueryString(Message message)
    {
        String queryString = ((HttpRequestAttributes)message.getAttributes()).getQueryString();
        return queryString == null ? "" : queryString;
    }

    public static String rewriteBaseUri(String raml, String baseSchemeHostPort)
    {
        return replaceBaseUri(raml, "https?://[^/]*", baseSchemeHostPort);
    }

    public static String replaceBaseUri(String raml, String newBaseUri)
    {
        return replaceBaseUri(raml, "https?://.*$", newBaseUri);
    }

    private static String replaceBaseUri(String raml, String regex, String replacement)
    {
        String[] split = raml.split("\n");
        for (int i=0; i<split.length; i++)
        {
            if (split[i].startsWith("baseUri: "))
            {
                split[i] = split[i].replaceFirst(regex, replacement);
            }
        }
        return StringUtils.join(split, "\n");
    }
}
