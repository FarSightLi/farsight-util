package org.example.performance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.performance.mapper.UserAccountMapper;
import org.example.performance.pojo.po.UserAccount;
import org.example.performance.service.UserAccountService;
import org.springframework.stereotype.Service;

/**
 * @author bonree
 * @description 针对表【user_account(用户账号密码表)】的数据库操作Service实现
 * @createDate 2023-12-18 16:53:27
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount>
        implements UserAccountService {

}




