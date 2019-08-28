package org.jeecg.modules.ticket.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.apache.bcel.classfile.Module.Require;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.ticket.entity.BisTicket;
import org.jeecg.modules.ticket.service.IBisTicketService;
import org.jeecg.modules.ticket.vo.ApproveVO;
import org.jeecg.modules.ticket.vo.CounterSignVO;
import org.jeecg.modules.ticket.vo.ManagerAdaptVO;
import org.jeecg.modules.ticket.vo.ManagerReviewVO;
import org.jeecg.modules.ticket.vo.OperateVO;
import org.jeecg.modules.ticket.vo.Outcome;

import java.util.Date;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 业务请示
 * @Author: jeecg-boot
 * @Date:   2019-08-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags="业务请示")
@RestController
@RequestMapping("/ticket/bisTicket")
public class BisTicketController {
	@Autowired
	private IBisTicketService bisTicketService;
	
	/**
	  * 分页列表查询
	 * @param bisTicket
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "业务请示-分页列表查询")
	@ApiOperation(value="业务请示-分页列表查询", notes="业务请示-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BisTicket>> queryPageList(BisTicket bisTicket,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  HttpServletRequest req) {
		Result<IPage<BisTicket>> result = new Result<IPage<BisTicket>>();
		QueryWrapper<BisTicket> queryWrapper = QueryGenerator.initQueryWrapper(bisTicket, req.getParameterMap());
		Page<BisTicket> page = new Page<BisTicket>(pageNo, pageSize);
		IPage<BisTicket> pageList = bisTicketService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  * 起草
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-起草")
	@ApiOperation(value="业务请示-起草", notes="业务请示-起草")
	@PostMapping(value = "/draft")
	public Result<BisTicket> draft(@RequestBody BisTicket bisTicket) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.draft(bisTicket);
			result.success("起草成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 提交
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-提交")
	@ApiOperation(value="业务请示-提交", notes="业务请示-提交")
	@PostMapping(value = "/submit")
	public Result<BisTicket> submit(@RequestBody BisTicket bisTicket) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.submit(bisTicket);
			result.success("提交成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 审核
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-审核")
	@ApiOperation(value="业务请示-审核", notes="业务请示-审核")
	@PostMapping(value = "/review")
	public Result<BisTicket> review(@RequestParam("ticketId") String ticketId, 
			@RequestParam("reviewerId") String reviewerId, 
			@RequestParam("pass") Boolean pass, 
			@RequestParam(value = "comment", required = false) String comment) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.review(ticketId, reviewerId, pass, comment);
			result.success("审核成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 部门负责人审核
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-部门负责人审核")
	@ApiOperation(value="业务请示-部门负责人审核", notes="业务请示-部门负责人审核")
	@PostMapping(value = "/managerReview")
	public Result<BisTicket> managerReview(@RequestBody ManagerReviewVO managerReviewVO) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.managerReview(managerReviewVO);
			result.success("部门负责人审核成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 会签操作
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-会签操作")
	@ApiOperation(value="业务请示-会签操作", notes="业务请示-会签操作")
	@PostMapping(value = "/counterSign")
	public Result<BisTicket> counterSign(@RequestBody CounterSignVO counterSignVO) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.sign(counterSignVO);
			result.success("会签操作成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 负责人调整建议
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-负责人调整建议")
	@ApiOperation(value="业务请示-负责人调整建议", notes="业务请示-负责人调整建议")
	@PostMapping(value = "/managerAdapt")
	public Result<BisTicket> managerAdapt(@RequestBody ManagerAdaptVO managerAdaptVO) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.managerAdapt(managerAdaptVO);
			result.success("负责人调整建议成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 操作
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-操作")
	@ApiOperation(value="业务请示-操作", notes="业务请示-操作")
	@PostMapping(value = "/operate")
	public Result<BisTicket> operate(@RequestBody OperateVO operateVO) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.operate(operateVO);
			result.success("操作成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 调整
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-调整")
	@ApiOperation(value="业务请示-调整", notes="业务请示-调整")
	@PostMapping(value = "/adapt")
	public Result<BisTicket> managerAdapt(@RequestBody BisTicket bisTicket) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.drafterAdapt(bisTicket);
			result.success("调整操作成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 高层批阅
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-高层批阅")
	@ApiOperation(value="业务请示-高层批阅", notes="业务请示-高层批阅")
	@PostMapping(value = "/approve")
	public Result<BisTicket> approve(@RequestBody ApproveVO approveVO) {
		Result<BisTicket> result = new Result<BisTicket>();
		try {
			bisTicketService.approve(approveVO);
			result.success("批阅操作成功！");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 获取后续操作
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-获取后续操作")
	@ApiOperation(value="业务请示-获取后续操作", notes="业务请示-获取后续操作")
	@GetMapping(value = "/outcomes")
	public Result<List<Outcome>> outcomes(@RequestParam("ticketId") String ticketId, 
			@RequestParam("takerId") String takerId) {
		Result<List<Outcome>> result = new Result<List<Outcome>>();
		try {
			List<Outcome> outcomeStrings = bisTicketService.findOutComeListByTaskId(ticketId, takerId);
			result.success("获取后续操作成功！");
			result.setResult(outcomeStrings);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 待处理任务
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-待处理任务")
	@ApiOperation(value="业务请示-待处理任务", notes="业务请示-待处理任务")
	@GetMapping(value = "/taskList")
	public Result<List<BisTicket>> taskList(@RequestParam("userId") String userId) {
		Result<List<BisTicket>> result = new Result<>();
		try {
			List<BisTicket> taskList = bisTicketService.findTaskListByUserId(userId);
			result.success("获取后续操作成功！");
			result.setResult(taskList);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 当前任务节点
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-当前任务节点")
	@ApiOperation(value="业务请示-当前任务节点", notes="业务请示-当前任务节点")
	@GetMapping(value = "/currentAct")
	public Result<Map<String, String>> currentAct(@RequestParam("ticketId") String ticketId, 
			@RequestParam("takerId") String takerId) {
		Result<Map<String, String>> result = new Result<>();
		try {
			Map<String, String> currentAct = bisTicketService.getCurrentAction(ticketId, takerId);
			result.success("获取当前任务节点成功！");
			result.setResult(currentAct);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 会签部门列表
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-会签部门列表")
	@ApiOperation(value="业务请示-会签部门列表", notes="业务请示-会签部门列表")
	@GetMapping(value = "/signDeptList")
	public Result<List<SysDepart>> signDeptList() {
		Result<List<SysDepart>> result = new Result<>();
		try {
			List<SysDepart> departList = bisTicketService.getSignDeptList();
			result.success("获取后续操作成功！");
			result.setResult(departList);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 有效承办人列表
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-有效承办人列表")
	@ApiOperation(value="业务请示-有效承办人列表", notes="业务请示-有效承办人列表")
	@GetMapping(value = "/validSignTaker")
	public Result<List<SysUser>> validSignTaker(String ticketId, String takerId) {
		Result<List<SysUser>> result = new Result<>();
		try {
			List<SysUser> takerList = bisTicketService.getValidSignTakerList(ticketId, takerId);
			result.success("获取有效承办人列表成功！");
			result.setResult(takerList);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 是否需要指定承办人
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-是否需要指定承办人")
	@ApiOperation(value="业务请示-是否需要指定承办人", notes="业务请示-是否需要指定承办人")
	@GetMapping(value = "/assignNeeded")
	public Result<Boolean> assignNeeded(String ticketId, String takerId) {
		Result<Boolean> result = new Result<>();
		try {
			Boolean assignNeeded = bisTicketService.assignNeeded(ticketId, takerId);
			result.success("是否需要指定承办人获取成功！");
			result.setResult(assignNeeded);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 部门负责人
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-会签部门负责人")
	@ApiOperation(value="业务请示-会签部门负责人", notes="业务请示-会签部门负责人")
	@GetMapping(value = "/signDeptManagers")
	public Result<String> signDeptManagers(@RequestParam("departs") String departs) {
		Result<String> result = new Result<>();
		if(StringUtils.isEmpty(departs)) {
			return result.error500("部门为空");
		}
		
		try {
			String managers = bisTicketService.getSignDeptManagers(departs);
			result.success("获取后续操作成功！");
			result.setResult(managers);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  * 副总裁列表
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-副总裁列表")
	@ApiOperation(value="业务请示-副总裁列表", notes="业务请示-副总裁列表")
	@GetMapping(value = "/vpList")
	public Result<List<SysUser>> vpList() {
		Result<List<SysUser>> result = new Result<>();
		
		try {
			List<SysUser> vPList = bisTicketService.getVPList();
			result.success("获取后续操作成功！");
			result.setResult(vPList);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("操作失败");
		}
		return result;
	}
	
	/**
	  *  编辑
	 * @param bisTicket
	 * @return
	 */
	@AutoLog(value = "业务请示-编辑")
	@ApiOperation(value="业务请示-编辑", notes="业务请示-编辑")
	@PutMapping(value = "/edit")
	public Result<BisTicket> edit(@RequestBody BisTicket bisTicket) {
		Result<BisTicket> result = new Result<BisTicket>();
		BisTicket bisTicketEntity = bisTicketService.getById(bisTicket.getId());
		if(bisTicketEntity==null) {
			result.error500("未找到对应实体");
		}else {
			boolean ok = bisTicketService.updateById(bisTicket);
			//TODO 返回false说明什么？
			if(ok) {
				result.success("修改成功!");
			}
		}
		
		return result;
	}
	
	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务请示-通过id删除")
	@ApiOperation(value="业务请示-通过id删除", notes="业务请示-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		try {
			bisTicketService.removeById(id);
		} catch (Exception e) {
			log.error("删除失败",e.getMessage());
			return Result.error("删除失败!");
		}
		return Result.ok("删除成功!");
	}
	
	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "业务请示-批量删除")
	@ApiOperation(value="业务请示-批量删除", notes="业务请示-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<BisTicket> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<BisTicket> result = new Result<BisTicket>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("参数不识别！");
		}else {
			this.bisTicketService.removeByIds(Arrays.asList(ids.split(",")));
			result.success("删除成功!");
		}
		return result;
	}
	
	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@AutoLog(value = "业务请示-通过id查询")
	@ApiOperation(value="业务请示-通过id查询", notes="业务请示-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BisTicket> queryById(@RequestParam(name="id",required=true) String id) {
		Result<BisTicket> result = new Result<BisTicket>();
		BisTicket bisTicket = bisTicketService.getById(id);
		if(bisTicket==null) {
			result.error500("未找到对应实体");
		}else {
			result.setResult(bisTicket);
			result.setSuccess(true);
		}
		return result;
	}

  /**
      * 导出excel
   *
   * @param request
   * @param response
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, HttpServletResponse response) {
      // Step.1 组装查询条件
      QueryWrapper<BisTicket> queryWrapper = null;
      try {
          String paramsStr = request.getParameter("paramsStr");
          if (oConvertUtils.isNotEmpty(paramsStr)) {
              String deString = URLDecoder.decode(paramsStr, "UTF-8");
              BisTicket bisTicket = JSON.parseObject(deString, BisTicket.class);
              queryWrapper = QueryGenerator.initQueryWrapper(bisTicket, request.getParameterMap());
          }
      } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
      }

      //Step.2 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      List<BisTicket> pageList = bisTicketService.list(queryWrapper);
      //导出文件名称
      mv.addObject(NormalExcelConstants.FILE_NAME, "业务请示列表");
      mv.addObject(NormalExcelConstants.CLASS, BisTicket.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("业务请示列表数据", "导出人:Jeecg", "导出信息"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
  }

  /**
      * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          MultipartFile file = entity.getValue();// 获取上传文件对象
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<BisTicket> listBisTickets = ExcelImportUtil.importExcel(file.getInputStream(), BisTicket.class, params);
              bisTicketService.saveBatch(listBisTickets);
              return Result.ok("文件导入成功！数据行数:" + listBisTickets.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.ok("文件导入失败！");
  }

}
