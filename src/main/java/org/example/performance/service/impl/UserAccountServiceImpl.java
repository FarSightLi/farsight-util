package org.example.performance.service.impl;

import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.UserAccountMapper;
import org.example.performance.pojo.po.UserAccount;
import org.example.performance.service.UserAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【user_account(用户账号密码表)】的数据库操作Service实现
 * @createDate 2023-12-18 16:53:27
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
        implements UserAccountService {
    @Value("${aes.key}")
    private String key;
    @Value("${aes.iv}")
    private String iv;

    @Override
    public String aesToMd5(String aesPwd) {
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
        String pwd = aes.decryptStr(aesPwd);
        return DigestUtil.md5Hex(pwd);
    }
}




