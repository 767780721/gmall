package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1OTk0NzQwNzl9.dhW751KXNwL3Vu1zjG1Abh2rEzu-XhIaWoH_f9vT_v0YSc0ayrF1xm5_blV_x5846MosSlYkNtzR9BbFHxYnViH5xISRckvytvR36CfiIwEQ_nbJSSU0W9DD6aqkKtOzpfR0e6mBe7-LeoBjLQxSajeT86L3L_5bHJ-HwXmQc3WWQ_YKfcc2FeYk1jveNdFviKEGTqiu0Zi7KVS0OevazRacxJKSI4Jl8Ao0kv_8L0GlS6ew9WykS9lwnhdbG9lUxzx3ywr2Dc9L5mquFU1y6wJ31TJg930EgmF3-XtapiPNd-po67jfpws4MxjWkWxlUeKDN0BBfqRJ02ytn7vvgg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}