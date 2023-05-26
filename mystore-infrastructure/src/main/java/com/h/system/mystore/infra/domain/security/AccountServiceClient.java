package com.h.system.mystore.infra.domain.security;

import com.h.system.mystore.infra.domain.account.Account;
import com.h.spring.boot.annotation.IstioClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * 用户信息的远程服务客户端
 * 各个工程都可能涉及取当前用户之类的操作，将此客户端放到基础包以便通用
 *
 * @author houjiahao
 * @date 2020/4/18 12:33
 **/
@IstioClient(contextId = "account")
public interface AccountServiceClient {

    @GET
    @Path("/restful/accounts/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    Account findByUsername(@PathParam("username") String username);
}
