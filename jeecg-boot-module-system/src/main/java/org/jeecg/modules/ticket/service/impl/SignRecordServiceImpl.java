package org.jeecg.modules.ticket.service.impl;

import org.jeecg.modules.ticket.entity.SignRecord;
import org.jeecg.modules.ticket.mapper.SignRecordMapper;
import org.jeecg.modules.ticket.service.ISignRecordService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 会签记录
 * @Author: jeecg-boot
 * @Date:   2019-08-26
 * @Version: V1.0
 */
@Service
public class SignRecordServiceImpl extends ServiceImpl<SignRecordMapper, SignRecord> implements ISignRecordService {

}
