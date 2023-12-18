package org.example.performance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.performance.pojo.po.UserAccount;

/**
 * @author bonree
 * @description 针对表【user_account(用户账号密码表)】的数据库操作Mapper
 * @createDate 2023-12-18 16:53:27
 * @Entity org.example.performance.pojo.po.UserAccount
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

}




