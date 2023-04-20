/*
 * Copyright 2012-2020. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. More information from:
 *
 *        https://github.com/fenixsoft
 */

package com.github.fenixsoft.bookstore.domain.security;

/**
 * 角色常量类
 * 目前系统中只有2种角色：用户，管理员
 * 为了注解{@link javax.annotation.security.RolesAllowed}中使用方便，定义为字符串常量（非枚举）
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/16 11:32
 **/
public interface Role {
    String USER = "ROLE_USER";
    String ADMIN = "ROLE_ADMIN";
}
