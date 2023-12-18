package org.example.performance.demo;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

/**
 * @author lilongsheng
 * @version 1.0
 * @project performance
 * @description test
 * @date 2023/12/18 16:40:37
 */
public class Test {
    @org.junit.jupiter.api.Test
    public void test1() {
        String text = "farsight";
        // key：AES模式下，key必须为16位
        String key = "1234567812345678";
        //iv：偏移量，ECB模式不需要，CBC模式下必须为16位
        String iv = "1234567812345678";
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
        //加密并进行Base转码
        String encrypt = aes.encryptBase64(text);
        System.out.println(encrypt);
        //解密为字符串
        String decrypt = aes.decryptStr(encrypt);
        System.out.println(decrypt);
    }
}
