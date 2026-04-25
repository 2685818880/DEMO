package cn.zdjc.wms.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 拦截Vue Router history模式路径，转发到Vue应用入口
 */
@Controller
public class VueController {

	@RequestMapping(value = "/wms/view/**")
	public String view() {
		return "/wms/index/main";
	}

	/**
	 * 处理Vue Router history模式路由（base: /app/wms/view/）
	 * 将所有/app/wms/view/下的路径转发到Vue入口index.html
	 */
	@RequestMapping(value = "/app/wms/view/**")
	public String forwardToVueApp() {
		return "forward:/app/index.html";
	}

	/**
	 * 处理AI管理页面Vue Router history模式路由
	 */
	@RequestMapping(value = {"/app/ai/**", "/app/ai-external"})
	public String forwardToAiViews() {
		return "forward:/app/index.html";
	}

}
