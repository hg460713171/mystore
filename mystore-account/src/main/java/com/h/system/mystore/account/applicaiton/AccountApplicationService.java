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

package com.h.system.mystore.account.applicaiton;

import com.h.system.mystore.account.domain.AccountRepository;
import com.h.system.mystore.infra.domain.account.Account;
import com.h.system.mystore.infra.infrastructure.utility.Encryption;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * 用户资源的应用服务接口
 *
 * @author houjiahao
 * @date 2020/3/10 17:46
 **/
@Named
@Transactional
public class AccountApplicationService {

    @Inject
    private AccountRepository repository;

    @Inject
    private Encryption encoder;

    public void createAccount(Account account) {
        account.setPassword(encoder.encode(account.getPassword()));
        repository.save(account);
    }

    public Account findAccountByUsername(String username) {
        return repository.findByUsername(username);
    }

    public void updateAccount(Account account) {
        repository.save(account);
    }

}
