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

package com.h.system.mystore.infra.warehouse.domain;

import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.CrudRepository;

/**
 * 广告对象数据仓库
 *
 * @author houjiahao
 * @date 2023/3/7 10:51
 **/
public interface AdvertisementRepository extends CrudRepository<Advertisement, Integer> {
    Iterable<Advertisement> findAll() throws DataAccessException;
}
