package com.egojit.easyweb.upms.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.egojit.easyweb.common.base.BaseResult;
import com.egojit.easyweb.common.base.BaseResultCode;
import com.egojit.easyweb.common.base.BaseWebController;
import com.egojit.easyweb.common.base.Page;
import com.egojit.easyweb.common.models.User;
import com.egojit.easyweb.common.utils.StringUtils;
import com.egojit.easyweb.upm.service.SysOfficeService;
import com.egojit.easyweb.upms.model.SysOffice;
import com.egojit.easyweb.upms.sso.UserUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Description：机构控制器
 * Auther：高露
 * Q Q:408365330
 * Company: 鼎斗信息技术有限公司
 * Time:2018-4-25
 */
@Controller
@RequestMapping("/admin/office")
@Api(value = "机构管理", description = "机构管理")
public class OfficeController extends BaseWebController{

    @Autowired
    private SysOfficeService service;

    @RequestMapping("/index")
    @ApiOperation(value = "机构管理首页")
    @RequiresPermissions("upms:office:read")
    public String index(){
        return "/upms/office/index";
    }

    @ResponseBody
    @PostMapping("/delete")
    @ApiOperation(value = "机构管理-删除接口")
    @RequiresPermissions("upms:office:delete")
    public BaseResult delete(String ids){
        BaseResult result=new BaseResult(BaseResultCode.SUCCESS,"删除成功");
        List<String> idList= JSON.parseArray(ids,String.class);
        int count= service.deleteByIds(idList);
        _log.info("删除了："+count+"数据");
        return result;
    }

    @RequestMapping("/edit")
    @ApiOperation(value = "机构管理-编辑界面")
    @RequiresPermissions("upms:office:edit")
    public String add(){
        return "/upms/office/edit";
    }

    @ApiOperation(value = "机构管理-编辑接口")
    @PostMapping("/edit")
    @ResponseBody
    @RequiresPermissions("upms:office:edit")
    public BaseResult edit(SysOffice model){
        BaseResult result=new BaseResult(BaseResultCode.SUCCESS,"成功");
        User curentUser= UserUtils.getUser();
        if(StringUtils.isEmpty(model.getParentId())){
            model.setParentId("1");
        }
        if(StringUtils.isEmpty(model.getId())){
            model.setCreateBy(curentUser.getId());
            model.setUpdateBy(curentUser.getId());
            service.insert(model);
        }else {
            model.setUpdateBy(curentUser.getId());
            service.updateByPrimaryKeySelective(model);
        }
        return result;
    }
    @RequestMapping("/detail")
    @ApiOperation(value = "机构管理-详情")
    @RequiresPermissions("upms:office:read")
    public String detail(){
        return "/upms/office/detail";
    }


    @ApiOperation(value = "机构管理-详情接口")
    @PostMapping("/detail")
    @ResponseBody
    @RequiresPermissions("upms:office:read")
    public BaseResult detail(String id){
        BaseResult result=new BaseResult(BaseResultCode.SUCCESS,"成功");
        SysOffice model=  service.selectByPrimaryKey(id);
        result.setData(model);
        return result;
    }


    /**
     * 获取所有机构列表
     * @param model
     * @return
     */
    @PostMapping("/list")
    @ResponseBody
    public BaseResult list(HttpServletRequest request,
                            HttpServletResponse response, SysOffice model) {
        Example example = new Example(SysOffice.class);
        Example.Criteria criteria = example.createCriteria();
        User loginUser=UserUtils.getUser();
        if (!StringUtils.isEmpty(model.getName())) {
            criteria.andLike("name", "%" + model.getName() + "%");
        }
        if (StringUtils.isEmpty(model.getParentId())) {
            if(!loginUser.isAdmin()){
                SysOffice office= service.selectByPrimaryKey(loginUser.getCompany().getId());
                criteria.andEqualTo("parentId",office.getParentId());
            }else{
                criteria.andEqualTo("parentId","1");
            }
        }else {
            criteria.andEqualTo("parentId",model.getParentId());
        }
        List<SysOffice> list = service.selectByExample(example);
        return new BaseResult(BaseResultCode.SUCCESS, list);
    }


    /**
     * 获取所有机构列表
     * @param model
     * @return
     */
    @PostMapping("/index")
    @ResponseBody
    public Page<SysOffice> index(HttpServletRequest request,
                           HttpServletResponse response, SysOffice model) {
        Page<SysOffice> pg = new Page<SysOffice>(request, response,-1);

        Example example = new Example(SysOffice.class);
        Example.Criteria criteria = example.createCriteria();
        User loginUser=UserUtils.getUser();
        if (!StringUtils.isEmpty(model.getName())) {
            criteria.andLike("name", "%" + model.getName() + "%");
        }
        if (StringUtils.isEmpty(model.getParentId())) {
            if(!loginUser.isAdmin()){
                SysOffice office= service.selectByPrimaryKey(loginUser.getCompany().getId());
                criteria.andEqualTo("parentId",office.getParentId());
            }else{
                criteria.andEqualTo("parentId","0");
            }
        }else {
            criteria.andEqualTo("parentId",model.getParentId());
        }
        pg = service.selectPageByExample(example, pg);
        return pg;
    }


    /**
     * 获取所有机构列表
     * @return
     */
    @ApiOperation(value = "机构管理-树层级结构接口")
    @PostMapping("/tree")
    @ResponseBody
    public JSONArray tree(SysOffice model) {
        Example example = new Example(SysOffice.class);
        Example.Criteria criteria = example.createCriteria();
        JSONArray list=new JSONArray();
        if (!StringUtils.isEmpty(model.getName())) {
            criteria.andLike("name", "%" + model.getName() + "%");
        }

        if (StringUtils.isEmpty(model.getId())) {
            User loginUser=UserUtils.getUser();
            if(!loginUser.isAdmin()){
                SysOffice office= service.selectByPrimaryKey(loginUser.getCompany().getId());
                criteria.andEqualTo("parentId",office.getParentId());
            }else{
                criteria.andEqualTo("parentId","0");
            }

        }else {
            criteria.andEqualTo("parentId",model.getId());
        }
        List<SysOffice> midList = service.selectByExample(example);
        if(midList!=null){
            for (SysOffice item:midList) {
                JSONObject  obj=new JSONObject();
                obj.put("name",item.getName());
                obj.put("id",item.getId());
                obj.put("pId",item.getParentId());
                obj.put("pIds",item.getParentIds());
                obj.put("isParent",""+isHaveChild(item.getId()));
                list.add(obj);
            }
        }
        return list;
    }


    /**
     * 判断是否有子部门
     * @param id
     * @return
     */
    public boolean isHaveChild(String id){
        Example example = new Example(SysOffice.class);
        example.createCriteria().andEqualTo("parentId",id);
        int count= service.selectCountByExample(example);
        return  count>0?true:false;
    }

    /**
     * 获取所有公司列表
     * @param model
     * @return
     */
    @PostMapping("/getAllCompanys")
    @ResponseBody
    public BaseResult getAllCompanys(HttpServletRequest request,
                           HttpServletResponse response, SysOffice model) {
        Example example = new Example(SysOffice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type","1");
        List<SysOffice> list = service.selectByExample(example);
        return new BaseResult(BaseResultCode.SUCCESS, list);
    }
    /**
     * 获取所有树形公司
     * @param model
     * @return
     */
    @PostMapping("/getCompanysTree")
    @ResponseBody
    public JSONArray getCompanysTree(HttpServletRequest request,
                                     HttpServletResponse response, SysOffice model) {
        Example example = new Example(SysOffice.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type","1");
        List<SysOffice> list = service.selectByExample(example);
        JSONArray array= getChilds(list,"0");
        return array;
    }



    /**
     * 获取子节点
     * @param list
     * @param pId
     * @return
     */
    private JSONArray getChilds(List<SysOffice> list,String pId){
        JSONArray array=new JSONArray();
        for (SysOffice item:list){
            if(pId.equals(item.getParentId())){
                JSONObject  obj=new JSONObject();
                obj.put("name",item.getName());
                obj.put("id",item.getId());
                obj.put("pId",item.getParentId());
                obj.put("pIds",item.getParentIds());
                boolean isHaveChild=isHaveChild(list,item.getId());
                obj.put("isParent",""+isHaveChild);
                if(isHaveChild){
                    obj.put("children",getChilds(list,item.getId()));
                }
                array.add(obj);
            }
        }
        return array;
    }

    /**
     * 判断是否有子部门
     * @param id
     * @return
     */
    public boolean isHaveChild(List<SysOffice> list,String id){
        for (SysOffice item:list){
            if(id.equals(item.getParentId())){
                return true;
            }
        }
        return  false;
    }


}
