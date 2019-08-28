package org.jeecg.modules.ticket.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 会签记录
 * @Author: jeecg-boot
 * @Date: 2019-08-26
 * @Version: V1.0
 */
@Data
@TableName("sign_record")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "sign_record对象", description = "会签记录")
public class SignRecord {

	/** createTime */
	@Excel(name = "createTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "createTime")
	private java.util.Date createTime;
	/** executionId */
	@Excel(name = "executionId", width = 15)
	@ApiModelProperty(value = "executionId")
	private java.lang.String executionId;
	/** id */
	@TableId(type = IdType.UUID)
	@ApiModelProperty(value = "id")
	private java.lang.String id;
	/** status */
	@Excel(name = "status", width = 15)
	@ApiModelProperty(value = "status")
	private java.lang.String status;
	/** takers */
	@Excel(name = "takers", width = 15)
	@ApiModelProperty(value = "takers")
	private java.lang.String takers;
	/** taskId */
	@Excel(name = "taskId", width = 15)
	@ApiModelProperty(value = "taskId")
	private java.lang.String taskId;
	/** ticketId */
	@Excel(name = "ticketId", width = 15)
	@ApiModelProperty(value = "ticketId")
	private java.lang.String ticketId;
	/** updateTime */
	@Excel(name = "updateTime", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "updateTime")
	private java.util.Date updateTime;
}
