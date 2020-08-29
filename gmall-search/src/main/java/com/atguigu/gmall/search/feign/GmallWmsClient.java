package com.atguigu.gmall.search.feign;

import com.atguigu.gamll.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
