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
 * @Description: 业务请示
 * @Author: jeecg-boot
 * @Date:   2019-08-20
 * @Version: V1.0
 */
@Data
@TableName("bis_ticket")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="bis_ticket对象", description="业务请示")
public class BisTicket implements Serializable {
	
	private static final long serialVersionUID = 1L;
    
	/**意见*/
	@Excel(name = "意见", width = 15)
    @ApiModelProperty(value = "意见")
	private java.lang.String comment;
	/**内容*/
	@Excel(name = "内容", width = 15)
    @ApiModelProperty(value = "内容")
	private java.lang.String content;
	/**创建时间*/
	@Excel(name = "创建时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
	private java.util.Date createTime;
	/**起草人*/
	@Excel(name = "起草人", width = 15)
    @ApiModelProperty(value = "起草人")
	private java.lang.String drafter;
	/**起草人ID*/
	@Excel(name = "起草人ID", width = 15)
    @ApiModelProperty(value = "起草人ID")
	private java.lang.String drafterId;
	/**ID*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "ID")
	private java.lang.String id;
	/**紧急级别*/
	@Excel(name = "紧急级别", width = 15)
    @ApiModelProperty(value = "紧急级别")
	private java.lang.String priority;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
	private java.lang.String remark;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
	private java.lang.String status;
	/**标题*/
	@Excel(name = "标题", width = 15)
    @ApiModelProperty(value = "标题")
	private java.lang.String title;
	/**请示类型*/
	@Excel(name = "请示类型", width = 15)
    @ApiModelProperty(value = "请示类型")
	private java.lang.String type;
	/**更新时间*/
	@Excel(name = "更新时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
	private java.util.Date updateTime;
}
