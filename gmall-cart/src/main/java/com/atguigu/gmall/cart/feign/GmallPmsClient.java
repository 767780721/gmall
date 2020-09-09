package com.atguigu.gmall.cart.feign;

import com.atguigu.gamll.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
