package com.h.spring.boot.FactoryBean;

/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import feign.Contract;
import feign.Feign;
import feign.Request;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs2.JAXRS2Contract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;

public class IstioClientFactoryBean implements FactoryBean<Object>, InitializingBean,
        ApplicationContextAware, BeanFactoryAware {

    /***********************************
     * WARNING! Nothing in this class should be @Autowired. It causes NPEs because of some
     * lifecycle race condition.
     ***********************************/

    private static Log LOG = LogFactory.getLog(IstioClientFactoryBean.class);

    private Class<?> type;

    private String name;

    private String url;

    private String contextId;

    private String path;

    private boolean decode404;

    private boolean inheritParentContext = true;

    private ApplicationContext applicationContext;

    private BeanFactory beanFactory;

    private int readTimeoutMillis = new Request.Options().readTimeoutMillis();

    private int connectTimeoutMillis = new Request.Options().connectTimeoutMillis();

    private boolean followRedirects = new Request.Options().isFollowRedirects();

    private Contract contract = new JAXRS2Contract();

    private Decoder decoder = new JacksonDecoder();

    private Encoder encoder = new JacksonEncoder();

    @Override
    public void afterPropertiesSet() {
        Assert.hasText(contextId, "Context id must be set");
//        Assert.hasText(name, "Name must be set");
    }


    @Override
    public Object getObject() {
        return Feign.builder().contract(contract).decoder(decoder).encoder(encoder).target(type, "http://"+contextId);
    }


    private String cleanPath() {
        String path = this.path.trim();
        if (StringUtils.hasLength(path)) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    @Override
    public Class<?> getObjectType() {
        return type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDecode404() {
        return decode404;
    }

    public void setDecode404(boolean decode404) {
        this.decode404 = decode404;
    }

    public boolean isInheritParentContext() {
        return inheritParentContext;
    }

    public void setInheritParentContext(boolean inheritParentContext) {
        this.inheritParentContext = inheritParentContext;
    }


    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        beanFactory = context;
    }





    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IstioClientFactoryBean that = (IstioClientFactoryBean) o;
        return Objects.equals(applicationContext, that.applicationContext)
                && Objects.equals(beanFactory, that.beanFactory)
                && decode404 == that.decode404
                && inheritParentContext == that.inheritParentContext
                && Objects.equals(name, that.name) && Objects.equals(path, that.path)
                && Objects.equals(type, that.type) && Objects.equals(url, that.url)
                && Objects.equals(connectTimeoutMillis, that.connectTimeoutMillis)
                && Objects.equals(readTimeoutMillis, that.readTimeoutMillis)
                && Objects.equals(followRedirects, that.followRedirects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationContext, beanFactory, decode404,
                inheritParentContext, name, path, type, url,
                readTimeoutMillis, connectTimeoutMillis, followRedirects);
    }

    @Override
    public String toString() {
        return new StringBuilder("FeignClientFactoryBean{").append("type=").append(type)
                .append(", ").append("name='").append(name).append("', ").append("url='")
                .append(url).append("', ").append("path='").append(path).append("', ")
                .append("decode404=").append(decode404).append(", ")
                .append("inheritParentContext=").append(inheritParentContext).append(", ")
                .append("applicationContext=").append(applicationContext).append(", ")
                .append("beanFactory=").append(beanFactory).append(", ")
                .append("connectTimeoutMillis=").append(connectTimeoutMillis).append("}")
                .append("readTimeoutMillis=").append(readTimeoutMillis).append("}")
                .append("followRedirects=").append(followRedirects).append("}")
                .toString();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
