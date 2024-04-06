package com.ccloud.main.controller;

import com.ccloud.main.config.jwt.pc.PcJwtUtil;
import com.ccloud.main.config.shiro.UserManager;
import com.ccloud.main.entity.BusinessUser;
import com.ccloud.main.logic.BusinessUserLogic;
import com.ccloud.main.pojo.enumeration.ResultEnum;
import com.ccloud.main.pojo.system.Result;
import com.ccloud.main.service.IBusinessUserService;
import com.ccloud.main.util.MD5Tools;
import com.ccloud.main.util.ResultUtil;
import com.ccloud.main.util.annotation.RequestJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * 登录控制器
 *
 * @author wangjie
 */
@RestController
@Slf4j
@Api(tags = {"登录注册"})
public class LoginController extends BaseController {

    @Resource
    private BusinessUserLogic businessUserLogic;

    @Resource
    private IBusinessUserService iBusinessUserService;

    @Resource
    private PcJwtUtil pcJwtUtil;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private DefaultKaptcha captchaProducer;

    private static final String VERIFY_CODE_KEY = "VERIFY_CODE";

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    @ApiOperation(value = "登录", notes = "登录", produces = "application/json")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名", required = true)
            , @ApiImplicitParam(name = "password", value = "密码", required = true)
    })
    public Result login(@RequestJson("username") String username, @RequestJson("password") String password) throws JsonProcessingException {

        BusinessUser user = businessUserLogic.findByName(username);
        if (user == null) {
            return ResultUtil.error(ResultEnum.USER_NOT_EXIST);
        }

        String password_salt = MD5Tools.parseStrToMd5L32(password + username);
        if (!password_salt.equals(user.getPassword())) {
            return ResultUtil.error(ResultEnum.USER_PASSWORD_ERROR);
        }
        user.setPassword("******");
        return ResultUtil.success(pcJwtUtil.sign(objectMapper.writeValueAsString(user)));
    }

    /**
     * 注册
     *
     * @param username
     * @param password
     * @param code     验证码
     * @return
     */
    @PostMapping("/reg")
    @ApiOperation("注册")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名", required = true)
            , @ApiImplicitParam(name = "password", value = "密码", required = true)
            , @ApiImplicitParam(name = "code", value = "验证码", required = true)
    })
    public Result reg(HttpServletRequest request, @RequestJson("username") String username, @RequestJson("password") String password, @RequestJson("code") String code) {

        String VERIFY_CODE = (String) request.getSession().getAttribute(VERIFY_CODE_KEY);
        if (!code.equals(VERIFY_CODE)) {
            return ResultUtil.error(ResultEnum.VERIFY_CODE_ERROR);
        }
        request.getSession().removeAttribute(VERIFY_CODE_KEY);

        BusinessUser user = businessUserLogic.findByName(username);
        if (user != null) {
            return ResultUtil.error(ResultEnum.USER_IS_EXIST);
        }
        businessUserLogic.register(username, password);
        return ResultUtil.success("注册成功");
    }


    @GetMapping("/verifyCode")
    @ApiOperation("获取验证码")
    public void verifyCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        byte[] captchaChallengeAsJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        try {
            //生产验证码字符串并保存到session中
            String createText = captchaProducer.createText();
            log.info("verifyCode:" + createText);
            httpServletRequest.getSession().setAttribute("code", createText);
            httpServletRequest.getSession().setAttribute(VERIFY_CODE_KEY, createText);
            //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = captchaProducer.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
        } catch (IllegalArgumentException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
        captchaChallengeAsJpeg = jpegOutputStream.toByteArray();

        httpServletResponse.setHeader("Cache-Control", "no-store");
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        httpServletResponse.setContentType("image/jpeg");
        ServletOutputStream responseOutputStream =
                httpServletResponse.getOutputStream();
        responseOutputStream.write(captchaChallengeAsJpeg);
        responseOutputStream.flush();
        responseOutputStream.close();

    }


    @PostMapping("/currUser")
    @RequiresPermissions("user:currUser")
    @ApiOperation("获取当前用户信息")
    public Result currUser() {
        return ResultUtil.success(UserManager.getCurrentUser());
    }


}
