package com.h.system.mystore.infra.warehouse;

import com.h.system.mystore.infra.resource.JAXRSResourceBase;
import org.junit.jupiter.api.Test;

/**
 * @author houjiahao
 * @date 2020/4/6 23:12
 **/
class AdvertisementResourceTest extends JAXRSResourceBase {

    @Test
    void getAllAdvertisements() {
        assertOK(get("/advertisements"));
    }
}
